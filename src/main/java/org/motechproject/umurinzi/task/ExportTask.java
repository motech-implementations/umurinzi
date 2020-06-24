package org.motechproject.umurinzi.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.dto.ExportField;
import org.motechproject.umurinzi.dto.ExportStatus;
import org.motechproject.umurinzi.helper.ExportFieldHelper;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.util.CustomByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ExportTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExportTask.class);

  private static final int MAX_EXPORT_BATCH_SIZE = 10000;

  private LookupService lookupService;

  @Getter
  private final UUID id;

  @Getter
  private ExportStatus status = ExportStatus.NOT_STARTED;

  @Getter
  private Double progress = 0.0;

  @Getter
  private CustomByteArrayOutputStream outputStream;

  private Class<?> entityDtoType;
  private Class<?> entityType;
  private String entityName;
  private List<ExportField> exportFields;
  private TableWriter tableWriter;
  private String lookup;
  private String lookupFields;
  private QueryParams queryParams;

  @Autowired
  public ExportTask(LookupService lookupService) {
    this.lookupService = lookupService;
    this.id = UUID.randomUUID();
  }

  //CHECKSTYLE:OFF: checkstyle:cyclomaticcomplexity
  @SuppressWarnings("checkstyle:CyclomaticComplexity")
  @Override
  public void run() { //NO CHECKSTYLE CyclomaticComplexity
    long batchCount = 0;
    status = ExportStatus.IN_PROGRESS;

    if (!queryParams.isPagingSet()) {
      long recordsCount;

      if (entityType != null) {
        recordsCount = lookupService.getEntitiesCount(entityType, lookup, lookupFields);
      } else {
        recordsCount = lookupService.getEntitiesCount(entityName, lookup, lookupFields);
      }

      batchCount = recordsCount / MAX_EXPORT_BATCH_SIZE;
    }

    List<String> headers = new ArrayList<>();

    for (ExportField exportField : exportFields) {
      headers.add(exportField.getDisplayName());
    }

    String[] fields = headers.toArray(new String[0]);

    try {
      tableWriter.writeHeader(fields);

      for (int i = 0; i <= batchCount && !ExportStatus.CANCELED.equals(status); i++) {
        progress = (double) i / (batchCount + 1);

        QueryParams newQueryParams;

        if (!queryParams.isPagingSet()) {
          newQueryParams = new QueryParams(i + 1, MAX_EXPORT_BATCH_SIZE, queryParams.getOrderList());
        } else {
          newQueryParams = queryParams;
        }

        List<?> entities;

        if (entityDtoType != null) {
          entities = lookupService.findEntities(entityDtoType, entityType, lookup, lookupFields, newQueryParams);
        } else if (entityType != null) {
          entities = lookupService.findEntities(entityType, lookup, lookupFields, newQueryParams);
        } else {
          entities = lookupService.findEntities(entityName, lookup, lookupFields, newQueryParams);
        }

        Map<String, String> row = new HashMap<>();

        for (Object entity : entities) {
          buildRow(row, entity, exportFields);
          tableWriter.writeRow(row, fields);
        }
      }
    } catch (Exception e) {
      status = ExportStatus.FAILED;
      outputStream = null;
      LOGGER.error("IO Error when writing data", e);
    } finally {
      tableWriter.close();

      if (ExportStatus.IN_PROGRESS.equals(status)) {
        status = ExportStatus.FINISHED;
      }

      entityDtoType = null;
      entityType = null;
      exportFields = null;
      tableWriter = null;
      lookup = null;
      lookupFields = null;
      queryParams = null;
    }
  }
  //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity

  public UUID setExportParams(CustomByteArrayOutputStream outputStream,  //NO CHECKSTYLE ParameterNumber
      Class<?> entityDtoType, Class<?> entityType, String entityName, List<ExportField> exportFields, TableWriter tableWriter,
      String lookup, String lookupFields, QueryParams queryParams) {
    this.outputStream = outputStream;
    this.entityDtoType = entityDtoType;
    this.entityType = entityType;
    this.entityName = entityName;
    this.exportFields = exportFields;
    this.tableWriter = tableWriter;
    this.lookup = lookup;
    this.lookupFields = lookupFields;
    this.queryParams = queryParams;

    return id;
  }

  public void cancel() {
    if (ExportStatus.IN_PROGRESS.equals(status) || ExportStatus.NOT_STARTED.equals(status)) {
      status = ExportStatus.CANCELED;
    }
  }

  public byte[] readData() {
    return outputStream.readDataAndReset();
  }

  private <T> void buildRow(Map<String, String> row, T entity, List<ExportField> exportFields) {
    row.clear();

    for (ExportField exportField : exportFields) {
      String fieldValue = ExportFieldHelper.getFieldValue(entity, exportField);
      row.put(exportField.getDisplayName(), fieldValue);
    }
  }
}
