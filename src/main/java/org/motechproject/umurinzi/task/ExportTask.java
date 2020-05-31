package org.motechproject.umurinzi.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.dto.ExportStatus;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.web.domain.Records;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ExportTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExportTask.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final int MAX_EXPORT_BATCH_SIZE = 1000;

  private LookupService lookupService;

  @Getter
  private final UUID id;

  @Getter
  private ExportStatus status = ExportStatus.NOT_STARTED;

  @Getter
  private Double progress = 0.0;

  @Getter
  private ByteArrayOutputStream outputStream;

  @Getter
  private String fileName;

  @Getter
  private String exportFormat;

  private Class<?> entityDtoType;
  private Class<?> entityType;
  private String entityName;
  private Map<String, String> headerMap;
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
    Records records;
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

    Set<String> keys = headerMap.keySet();
    String[] fields = keys.toArray(new String[0]);

    try {
      tableWriter.writeHeader(fields);

      for (int i = 0; i <= batchCount && !ExportStatus.CANCELED.equals(status); i++) {
        progress = (double) i / batchCount;

        QueryParams newQueryParams;

        if (!queryParams.isPagingSet()) {
          newQueryParams = new QueryParams(i + 1, MAX_EXPORT_BATCH_SIZE, queryParams.getOrderList());
        } else {
          newQueryParams = queryParams;
        }

        if (entityDtoType != null) {
          records = lookupService.getEntities(entityDtoType, entityType, lookup, lookupFields, newQueryParams);
        } else if (entityType != null) {
          records = lookupService.getEntities(entityType, lookup, lookupFields, newQueryParams);
        } else {
          records = lookupService.getEntities(entityName, lookup, lookupFields, newQueryParams);
        }

        List entities = records.getRows();

        for (Object entity : entities) {
          Map<String, String> row = buildRow(entity, headerMap);
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
      headerMap = null;
      tableWriter = null;
      lookup = null;
      lookupFields = null;
      queryParams = null;
    }
  }
  //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity

  public UUID setExportParams(ByteArrayOutputStream outputStream, String fileName, String exportFormat,  //NO CHECKSTYLE ParameterNumber
      Class<?> entityDtoType, Class<?> entityType, String entityName, Map<String, String> headerMap, TableWriter tableWriter,
      String lookup, String lookupFields, QueryParams queryParams) {
    this.outputStream = outputStream;
    this.fileName = fileName;
    this.exportFormat = exportFormat;
    this.entityDtoType = entityDtoType;
    this.entityType = entityType;
    this.entityName = entityName;
    this.headerMap = headerMap;
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

  private <T> Map<String, String> buildRow(T entity, Map<String, String> headerMap) throws IOException {
    String json = OBJECT_MAPPER.writeValueAsString(entity);
    Map<String, Object> entityMap = OBJECT_MAPPER.readValue(json, new TypeReference<HashMap>() {
    });
    Map<String, String> row = new LinkedHashMap<>();

    for (Entry<String, String> entry : headerMap.entrySet()) {
      String fieldName = entry.getValue();
      if (fieldName == null) {
        row.put(entry.getKey(), null);
        continue;
      }
      String[] fieldPath = fieldName.split("\\.");
      String value = null;
      if (fieldPath.length == 2) {
        Map objectMap = (Map) entityMap.get(fieldPath[0]);
        Object fieldValue = objectMap.get(fieldPath[1]);
        if (fieldValue != null) {
          value = fieldValue.toString();
        }
      } else {
        Object entryValue = entityMap.get(entry.getValue());
        if (entryValue != null) {
          value = entryValue.toString();
        }
      }
      row.put(entry.getKey(), value);
    }
    return row;
  }
}
