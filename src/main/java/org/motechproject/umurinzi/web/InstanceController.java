package org.motechproject.umurinzi.web;

import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.motechproject.umurinzi.constants.UmurinziConstants.APPLICATION_PDF_CONTENT;
import static org.motechproject.umurinzi.constants.UmurinziConstants.TEXT_CSV_CONTENT;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.service.impl.SubjectCsvImportCustomizer;
import org.motechproject.umurinzi.util.QueryParamsBuilder;
import org.motechproject.umurinzi.util.SubjectVisitsMixin;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;
import org.motechproject.mds.dto.CsvImportResults;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.ex.csv.CsvImportException;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.CsvImportExportService;
import org.motechproject.mds.service.EntityService;
import org.motechproject.mds.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class InstanceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceController.class);

    @Autowired
    private LookupService lookupService;

    @Autowired
    private CsvImportExportService csvImportExportService;

    @Autowired
    private SubjectCsvImportCustomizer subjectCsvImportCustomizer;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private EntityService entityService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize(UmurinziConstants.HAS_SUBJECTS_TAB_ROLE)
    @RequestMapping(value = "/instances/{entityId}/Participantcsvimport", method = RequestMethod.POST)
    @ResponseBody
    public long subjectImportCsv(@PathVariable long entityId, @RequestParam(required = true) MultipartFile csvFile) {
        return importCsv(entityId, csvFile);
    }

    @PreAuthorize(UmurinziConstants.HAS_SUBJECTS_TAB_ROLE)
    @RequestMapping(value = "/instances/Participant", method = RequestMethod.POST)
    @ResponseBody
    public String getParticipantInstances(GridSettings settings) throws IOException {
        String lookup = settings.getLookup();
        Map<String, Object> fieldMap = getFields(settings);

        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, fieldMap);

        Records<Subject> records = lookupService.getEntities(Subject.class, lookup, settings.getFields(), queryParams);

        ObjectMapper mapper = new ObjectMapper();

        mapper.getSerializationConfig().addMixInAnnotations(Subject.class, SubjectVisitsMixin.class);

        return mapper.writeValueAsString(records);
    }

    @RequestMapping(value = "/entities/{entityId}/exportInstances", method = RequestMethod.GET)
    public void exportEntityInstances(@PathVariable Long entityId, GridSettings settings,
                                      @RequestParam String exportRecords,
                                      @RequestParam String outputFormat,
                                      HttpServletResponse response) throws IOException {

        EntityDto entityDto = entityService.getEntity(entityId);
        String entityName = entityDto.getName();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

        final String fileName = entityName + "_" + DateTime.now().toString(dateTimeFormatter);

        if (Constants.ExportFormat.PDF.equals(outputFormat)) {
            response.setContentType(APPLICATION_PDF_CONTENT);
        } else {
            response.setContentType(TEXT_CSV_CONTENT);
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());

        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
                QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        if (Constants.ExportFormat.PDF.equals(outputFormat)) {
            response.setContentType(APPLICATION_PDF_CONTENT);

            csvImportExportService.exportPdf(entityId, response.getOutputStream(), settings.getLookup(), queryParams,
                    settings.getSelectedFields(), getFields(settings));
        } else if (UmurinziConstants.CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType(TEXT_CSV_CONTENT);

            csvImportExportService.exportCsv(entityId, response.getWriter(), settings.getLookup(), queryParams,
                    settings.getSelectedFields(), getFields(settings));
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
    }

    @RequestMapping(value = "/getLookupsForSubjects", method = RequestMethod.GET)
    @ResponseBody
    public List<LookupDto> getLookupsForSubjects() {
        List<LookupDto> ret = new ArrayList<>();
        List<LookupDto> availableLookups;
        try {
            availableLookups = lookupService.getAvailableLookups(Subject.class.getName());
        } catch (UmurinziLookupException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        List<String> lookupList = UmurinziConstants.AVAILABLE_LOOKUPS_FOR_SUBJECTS;
        for (LookupDto lookupDto : availableLookups) {
            if (lookupList.contains(lookupDto.getLookupName())) {
                ret.add(lookupDto);
            }
        }
        return ret;
    }

    @PreAuthorize("hasRole('manageUmurinzi')")
    @RequestMapping(value = "/subjectDataChanged", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> subjectDataChanged(@RequestBody Subject subject) {
        subjectService.subjectDataChanged(subject);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private long importCsv(long entityId, MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                CsvImportResults results = csvImportExportService.importCsv(entityId, reader,
                        csvFile.getOriginalFilename(), subjectCsvImportCustomizer);
                return results.totalNumberOfImportedInstances();
            }
        } catch (IOException e) {
            throw new CsvImportException("Unable to open uploaded file", e);
        }
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<LinkedHashMap>() {}); //NO CHECKSTYLE WhitespaceAround
        }
    }
}
