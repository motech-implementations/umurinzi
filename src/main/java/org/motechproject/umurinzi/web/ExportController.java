package org.motechproject.umurinzi.web;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.motechproject.umurinzi.constants.UmurinziConstants.APPLICATION_PDF_CONTENT;
import static org.motechproject.umurinzi.constants.UmurinziConstants.TEXT_CSV_CONTENT;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.IvrAndSmsStatisticReport;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.dto.IvrAndSmsStatisticReportDto;
import org.motechproject.umurinzi.dto.MissedVisitsReportDto;
import org.motechproject.umurinzi.dto.OptsOutOfMotechMessagesReportDto;
import org.motechproject.umurinzi.dto.VisitRescheduleDto;
import org.motechproject.umurinzi.exception.UmurinziExportException;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.helper.DtoLookupHelper;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.template.PdfBasicTemplate;
import org.motechproject.umurinzi.template.PdfExportTemplate;
import org.motechproject.umurinzi.util.QueryParamsBuilder;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ExportController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportController.class);

    private static final String PDF_EXPORT_FORMAT = "pdf";
    private static final String CSV_EXPORT_FORMAT = "csv";
    private static final String XLS_EXPORT_FORMAT = "xls";

    @Autowired
    private ExportService exportService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/exportInstances/visitReschedule", method = RequestMethod.GET)
    public void exportVisitReschedule(GridSettings settings, @RequestParam String exportRecords,
                                      @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        exportEntity(newSettings, exportRecords, outputFormat, response, UmurinziConstants.VISIT_RESCHEDULE_NAME,
                VisitRescheduleDto.class, Visit.class, UmurinziConstants.VISIT_RESCHEDULE_FIELDS_MAP);
    }

    @RequestMapping(value = "/exportDailyClinicVisitScheduleReport", method = RequestMethod.GET)
    public void exportDailyClinicVisitScheduleReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        exportEntity(settings, exportRecords, outputFormat, response, UmurinziConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_NAME,
            null, Visit.class, UmurinziConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_MAP);
    }

    @RequestMapping(value = "/exportFollowupsMissedClinicVisitsReport", method = RequestMethod.GET)
    public void exportFollowupsMissedClinicVisitsReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForFollowupsMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lookups params");
        } else {
            exportEntity(newSettings, exportRecords, outputFormat, response, UmurinziConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_NAME,
                MissedVisitsReportDto.class, Visit.class, UmurinziConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_MAP);
        }
    }

    @RequestMapping(value = "/exportMandEMissedClinicVisitsReport", method = RequestMethod.GET)
    public void exportMandEMissedClinicVisitsReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForMandEMissedClinicVisitsReport(settings);

        if (newSettings == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lookups params");
        } else {
            exportEntity(newSettings, exportRecords, outputFormat, response, UmurinziConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_NAME,
                MissedVisitsReportDto.class, Visit.class, UmurinziConstants.M_AND_E_MISSED_CLINIC_VISITS_REPORT_MAP);
        }
    }

    @RequestMapping(value = "/exportOptsOutOfMotechMessagesReport", method = RequestMethod.GET)
    public void exportOptsOutOfMotechMessagesReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        GridSettings newSettings = DtoLookupHelper.changeLookupAndOrderForOptsOutOfMotechMessagesReport(settings);

        if (newSettings == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid lookups params");
        } else {
            exportEntity(newSettings, exportRecords, outputFormat, response, UmurinziConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_NAME,
                OptsOutOfMotechMessagesReportDto.class, SubjectEnrollments.class, UmurinziConstants.OPTS_OUT_OF_MOTECH_MESSAGES_REPORT_MAP);
        }
    }

    @RequestMapping(value = "/exportIvrAndSmsStatisticReport", method = RequestMethod.GET)
    public void exportIvrAndSmsStatisticReport(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        exportEntity(settings, exportRecords, outputFormat, response, UmurinziConstants.IVR_AND_SMS_STATISTIC_REPORT_NAME,
            IvrAndSmsStatisticReportDto.class, IvrAndSmsStatisticReport.class, UmurinziConstants.IVR_AND_SMS_STATISTIC_REPORT_MAP);
    }

    @RequestMapping(value = "/exportSubjectEnrollment", method = RequestMethod.GET)
    public void exportSubjectEnrollment(GridSettings settings, @RequestParam String exportRecords,
        @RequestParam String outputFormat, HttpServletResponse response) throws IOException {

        exportEntity(settings, exportRecords, outputFormat, response, UmurinziConstants.SUBJECT_ENROLLMENTS_NAME,
            null, SubjectEnrollments.class, UmurinziConstants.SUBJECT_ENROLLMENTS_MAP);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) {
        LOGGER.error(e.getMessage(), e);
        return e.getMessage();
    }

    private void exportEntity(GridSettings settings, String exportRecords, String outputFormat, HttpServletResponse response, //NO CHECKSTYLE ParameterNumber
                              String fileNameBeginning, Class<?> entityDtoType, Class<?> entityType, Map<String, String> headerMap) throws IOException {

        setResponseData(response, outputFormat, fileNameBeginning);

        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
                QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        try {
            if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfBasicTemplate template = new PdfExportTemplate(response.getOutputStream());

                exportService.exportEntityToPDF(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
                exportService.exportEntityToCSV(response.getWriter(), entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
                exportService.exportEntityToExcel(response.getOutputStream(), entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            }
        } catch (IOException | UmurinziLookupException | UmurinziExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {
            }); //NO CHECKSTYLE WhitespaceAround
        }
    }

    private void setResponseData(HttpServletResponse response, String outputFormat, String fileNameBeginning) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        final String fileName = fileNameBeginning + "_" + DateTime.now().toString(dateTimeFormatter);

        if (PDF_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(APPLICATION_PDF_CONTENT);
        } else if (CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(TEXT_CSV_CONTENT);
        } else if (XLS_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("application/vnd.ms-excel");
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());
    }
}
