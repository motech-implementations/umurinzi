package org.motechproject.umurinzi.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.impl.csv.writer.CsvTableWriter;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.template.PdfBasicTemplate;
import org.motechproject.umurinzi.util.CustomColumnWidthPdfTableWriter;
import org.motechproject.umurinzi.util.ExcelTableWriter;
import org.motechproject.umurinzi.util.PdfTableWriter;
import org.motechproject.umurinzi.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("exportService")
public class ExportServiceImpl implements ExportService {

    @Autowired
    private LookupService lookupService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void exportEntityToPDF(PdfBasicTemplate template, Class<?> entityDtoType, Class<?> entityType,
                                  Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException {
        PdfTableWriter tableWriter = new CustomColumnWidthPdfTableWriter(template);
        exportEntity(entityDtoType, entityType, headerMap, tableWriter, lookup, lookupFields, queryParams);
    }

    @Override
    public void exportEntityToCSV(Writer writer, Class<?> entityDtoType, Class<?> entityType,
                                  Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException {
        CsvTableWriter tableWriter = new CsvTableWriter(writer);
        exportEntity(entityDtoType, entityType, headerMap, tableWriter, lookup, lookupFields, queryParams);
    }

    @Override
    public void exportEntityToExcel(OutputStream outputStream, Class<?> entityDtoType, Class<?> entityType,
                                    Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams)
            throws IOException {
        ExcelTableWriter tableWriter = new ExcelTableWriter(outputStream);
        exportEntity(entityDtoType, entityType, headerMap, tableWriter, lookup, lookupFields, queryParams);
    }

    @Override
    public <T> void exportEntity(List<T> entities, Map<String, String> headerMap, TableWriter tableWriter)
            throws IOException {
        exportEntityList(entities, headerMap, tableWriter);
    }

    private void exportEntity(Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap,
                              TableWriter tableWriter, String lookup, String lookupFields, QueryParams queryParams) throws IOException {
        Records records;
        if (entityDtoType != null) {
            records = lookupService.getEntities(entityDtoType, entityType, lookup, lookupFields, queryParams);
        } else {
            records = lookupService.getEntities(entityType, lookup, lookupFields, queryParams);
        }

        exportEntityList(records.getRows(), headerMap, tableWriter);
    }

    private void exportEntityList(List entities, Map<String, String> headerMap, TableWriter tableWriter)
            throws IOException {
        Set<String> keys = headerMap.keySet();
        String[] fields = keys.toArray(new String[keys.size()]);
        try {
            tableWriter.writeHeader(fields);
            for (Object entity : entities) {
                Map<String, String> row = buildRow(entity, headerMap);
                tableWriter.writeRow(row, fields);
            }
        } catch (IOException e) {
            throw new IOException("IO Error when writing data", e);
        } finally {
            tableWriter.close();
        }
    }

    private <T> Map<String, String> buildRow(T entity, Map<String, String> headerMap) throws IOException {
        String json = objectMapper.writeValueAsString(entity);
        Map<String, Object> entityMap = objectMapper.readValue(json, new TypeReference<HashMap>() {
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
