package org.motechproject.umurinzi.service;

import java.util.Map;
import java.util.UUID;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.umurinzi.dto.ExportResult;
import org.motechproject.umurinzi.dto.ExportStatusResponse;

public interface ExportService {

    UUID exportEntity(String outputFormat, String fileName, Class<?> entityDtoType, Class<?> entityType,
        Map<String, String> headerMap, String lookup, String lookupFields, QueryParams queryParams);

    ExportStatusResponse getExportStatus(UUID exportId);

    ExportResult getExportResults(UUID exportId);

    void cancelExport(UUID exportId);

    void cancelAllExportTasks();
}
