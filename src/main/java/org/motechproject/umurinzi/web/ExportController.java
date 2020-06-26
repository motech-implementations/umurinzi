package org.motechproject.umurinzi.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.IvrAndSmsStatisticReport;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.dto.ExportField;
import org.motechproject.umurinzi.dto.ExportStatusResponse;
import org.motechproject.umurinzi.dto.IvrAndSmsStatisticReportDto;
import org.motechproject.umurinzi.dto.MissedVisitsReportDto;
import org.motechproject.umurinzi.dto.OptsOutOfMotechMessagesReportDto;
import org.motechproject.umurinzi.dto.VisitRescheduleDto;
import org.motechproject.umurinzi.helper.DtoLookupHelper;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.util.QueryParamsBuilder;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);

    @Autowired
    private ExportService exportService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/export/{exportId}/status", method = RequestMethod.GET)
    public ResponseEntity<ExportStatusResponse> exportStatus(@PathVariable UUID exportId) {
        ExportStatusResponse exportStatus = exportService.getExportStatus(exportId);

        return new ResponseEntity<>(exportStatus, HttpStatus.OK);
    }

    @RequestMapping(value = "/export/{exportId}/cancel", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void exportCancel(@PathVariable UUID exportId) {
        exportService.cancelExport(exportId);
    }

    @RequestMapping(value = "/exportInstances/visitReschedule", method = RequestMethod.GET)
    public ResponseEntity<String> exportVisitReschedule(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, UmurinziConstants.VISIT_RESCHEDULE_NAME,
            VisitRescheduleDto.class, Visit.class, UmurinziConstants.VISIT_RESCHEDULE_FIELDS_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportDailyClinicVisitScheduleReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportDailyClinicVisitScheduleReport(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, UmurinziConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_NAME,
            null, Visit.class, UmurinziConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportFollowupsMissedClinicVisitsReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportFollowupsMissedClinicVisitsReport(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForFollowupsMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            return new ResponseEntity<>("Invalid lookups params", HttpStatus.BAD_REQUEST);
        }

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, UmurinziConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_NAME,
            MissedVisitsReportDto.class, Visit.class, UmurinziConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportMandEMissedClinicVisitsReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportMandEMissedClinicVisitsReportStart(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForMandEMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            return new ResponseEntity<>("Invalid lookups params", HttpStatus.BAD_REQUEST);
        }

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, UmurinziConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_NAME,
            MissedVisitsReportDto.class, Visit.class, UmurinziConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportOptsOutOfMotechMessagesReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportOptsOutOfMotechMessagesReport(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForOptsOutOfMotechMessagesReport(settings);

        UUID exportId = exportEntity(newSettings, exportRecords, outputFormat, UmurinziConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_NAME,
            OptsOutOfMotechMessagesReportDto.class, SubjectEnrollments.class, UmurinziConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportIvrAndSmsStatisticReport", method = RequestMethod.GET)
    public ResponseEntity<String> exportIvrAndSmsStatisticReport(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, UmurinziConstants.IVR_AND_SMS_STATISTIC_REPORT_NAME,
            IvrAndSmsStatisticReportDto.class, IvrAndSmsStatisticReport.class, UmurinziConstants.IVR_AND_SMS_STATISTIC_REPORT_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/exportSubjectEnrollment", method = RequestMethod.GET)
    public ResponseEntity<String> exportSubjectEnrollment(GridSettings settings,
        @RequestParam String exportRecords, @RequestParam String outputFormat) throws IOException {

        UUID exportId = exportEntity(settings, exportRecords, outputFormat, UmurinziConstants.SUBJECT_ENROLLMENTS_NAME,
            null, SubjectEnrollments.class, UmurinziConstants.SUBJECT_ENROLLMENTS_MAP);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }

    private UUID exportEntity(GridSettings settings, String exportRecords, String outputFormat,
        String fileNameBeginning, Class<?> entityDtoType, Class<?> entityType, List<ExportField> exportFields) throws IOException {

        QueryParams queryParams = new QueryParams(1,
            StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
            QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        return exportService.exportEntity(outputFormat, fileNameBeginning, entityDtoType,
            entityType, exportFields, settings.getLookup(), settings.getFields(), queryParams);
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }
}
