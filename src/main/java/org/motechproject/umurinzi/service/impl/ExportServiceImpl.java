package org.motechproject.umurinzi.service.impl;

import static org.motechproject.umurinzi.constants.UmurinziConstants.CSV_EXPORT_FORMAT;
import static org.motechproject.umurinzi.constants.UmurinziConstants.PDF_EXPORT_FORMAT;
import static org.motechproject.umurinzi.constants.UmurinziConstants.XLS_EXPORT_FORMAT;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.CsvTableWriter;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.dto.ExportField;
import org.motechproject.umurinzi.dto.ExportStatus;
import org.motechproject.umurinzi.dto.ExportStatusResponse;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.task.ExportTask;
import org.motechproject.umurinzi.util.CustomByteArrayOutputStream;
import org.motechproject.umurinzi.util.CustomColumnWidthPdfTableWriter;
import org.motechproject.umurinzi.util.ExcelTableWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("exportService")
public class ExportServiceImpl implements ExportService {

    private final Map<UUID, ExportTask> exportTaskMap = new HashMap<>();

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public UUID exportEntity(String outputFormat, String fileName, String entityName,
        List<ExportField> exportFields, String lookup, String lookupFields, QueryParams queryParams) {
        return exportEntity(outputFormat, fileName, null, null, entityName, exportFields, lookup, lookupFields, queryParams);
    }

    @Override
    public UUID exportEntity(String outputFormat, String fileName, Class<?> entityDtoType, Class<?> entityType,  //NO CHECKSTYLE ParameterNumber
        List<ExportField> exportFields, String lookup, String lookupFields, QueryParams queryParams) {
        return exportEntity(outputFormat, fileName, entityDtoType, entityType, null, exportFields, lookup, lookupFields, queryParams);
    }

    private UUID exportEntity(String outputFormat, String fileName, Class<?> entityDtoType, Class<?> entityType, String entityName,  //NO CHECKSTYLE ParameterNumber
        List<ExportField> exportFields, String lookup, String lookupFields, QueryParams queryParams) {

        CustomByteArrayOutputStream outputStream = new CustomByteArrayOutputStream();
        TableWriter tableWriter;

        if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
            tableWriter = new CustomColumnWidthPdfTableWriter(outputStream);
        } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
            Writer writer = new OutputStreamWriter(outputStream);
            tableWriter = new CsvTableWriter(writer);
        } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
            tableWriter = new ExcelTableWriter(outputStream);
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }

        ExportTask exportTask = applicationContext.getBean(ExportTask.class);

        UUID taskId = exportTask.setExportParams(outputStream, entityDtoType, entityType, entityName,
            exportFields, tableWriter, lookup, lookupFields, queryParams);
        exportTaskMap.put(taskId, exportTask);

        taskExecutor.execute(exportTask);

        return taskId;
    }

    @Override
    public ExportStatusResponse getExportStatus(UUID exportId) {
        return getTaskStatus(exportId);
    }

    @Override
    public void cancelExport(UUID exportId) {
        cancelAndRemoveTask(exportId);
    }

    @Override
    public void cancelAllExportTasks() {
        cancelAndRemoveAllTasks();
    }

    private synchronized ExportStatusResponse getTaskStatus(UUID taskId) {
        ExportTask exportTask = getTask(taskId);
        ExportStatus status = exportTask.getStatus();

        if (!ExportStatus.IN_PROGRESS.equals(status) && !ExportStatus.NOT_STARTED.equals(status)) {
            exportTaskMap.remove(taskId);
        }

        return new ExportStatusResponse(status, exportTask.getProgress(), exportTask.readData());
    }

    private synchronized void cancelAndRemoveTask(UUID taskId) {
        ExportTask exportTask = getTask(taskId);
        exportTask.cancel();

        exportTaskMap.remove(taskId);
    }

    private synchronized void cancelAndRemoveAllTasks() {
        for (ExportTask exportTask : exportTaskMap.values()) {
            exportTask.cancel();
        }

        exportTaskMap.clear();
    }

    private ExportTask getTask(UUID taskId) {
        if (!exportTaskMap.containsKey(taskId)) {
            throw new IllegalArgumentException("Export task with id: " + taskId.toString() + " not found");
        }

        return exportTaskMap.get(taskId);
    }
}
