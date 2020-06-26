package org.motechproject.umurinzi.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.dto.CsvImportResults;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.dto.FieldDto;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.dto.TypeDto;
import org.motechproject.mds.ex.csv.CsvImportException;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.CsvImportCustomizer;
import org.motechproject.mds.service.CsvImportExportService;
import org.motechproject.mds.service.EntityService;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Holiday;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.dto.ExportField;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.service.HolidayService;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.service.impl.HolidayCsvImportCustomizer;
import org.motechproject.umurinzi.service.impl.SubjectCsvImportCustomizer;
import org.motechproject.umurinzi.util.QueryParamsBuilder;
import org.motechproject.umurinzi.util.SubjectVisitsMixin;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;
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
    private ExportService exportService;

    @Autowired
    private SubjectCsvImportCustomizer subjectCsvImportCustomizer;

    @Autowired
    private HolidayCsvImportCustomizer holidayCsvImportCustomizer;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private EntityService entityService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PreAuthorize(UmurinziConstants.HAS_IMPORT_SUBJECTS_ROLE)
    @RequestMapping(value = "/instances/{entityId}/Participantcsvimport", method = RequestMethod.POST)
    @ResponseBody
    public long subjectImportCsv(@PathVariable long entityId, @RequestParam(required = true) MultipartFile csvFile) {
        return importCsv(entityId, csvFile, subjectCsvImportCustomizer);
    }

    @PreAuthorize(UmurinziConstants.HAS_IMPORT_SUBJECTS_ROLE)
    @RequestMapping(value = "/checkSubjectImportPermissions", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> checkSubjectImportPermissions() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_HOLIDAYS_ROLE)
    @RequestMapping(value = "/instances/{entityId}/Holidaycsvimport", method = RequestMethod.POST)
    @ResponseBody
    public long holidayImportCsv(@PathVariable long entityId, @RequestParam(required = true) MultipartFile csvFile) {
        return importCsv(entityId, csvFile, holidayCsvImportCustomizer);
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

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_HOLIDAYS_ROLE)
    @RequestMapping(value = "/instances/Holiday", method = RequestMethod.POST)
    @ResponseBody
    public String getHolidayInstances(GridSettings settings) throws IOException {
        String lookup = settings.getLookup();
        Map<String, Object> fieldMap = getFields(settings);

        QueryParams queryParams = QueryParamsBuilder.buildQueryParams(settings, fieldMap);

        Records<Holiday> records = lookupService.getEntities(Holiday.class, lookup, settings.getFields(), queryParams);

        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(records);
    }

    @RequestMapping(value = "/entities/{entityId}/exportInstances", method = RequestMethod.GET)
    public ResponseEntity<String> exportEntityInstances(@PathVariable Long entityId, GridSettings settings,
                                      @RequestParam String exportRecords,
                                      @RequestParam String outputFormat) throws IOException {

        EntityDto entityDto = entityService.getEntity(entityId);

        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords),
                QueryParamsBuilder.buildOrderList(settings, getFields(settings)));

        List<ExportField> exportFields = getExportFields(entityId, settings.getSelectedFields());

        UUID exportId = exportService.exportEntity(outputFormat, entityDto.getName(), entityDto.getClassName(),
            exportFields, settings.getLookup(), settings.getFields(), queryParams);

        return new ResponseEntity<>(exportId.toString(), HttpStatus.OK);
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

    @PreAuthorize(UmurinziConstants.HAS_MANAGE_HOLIDAYS_ROLE)
    @RequestMapping(value = "/holidayDataChanged", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> holidayDataChanged(@RequestBody Holiday holiday) {
        holidayService.dataChanged(holiday);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private long importCsv(long entityId, MultipartFile csvFile, CsvImportCustomizer csvImportCustomizer) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                CsvImportResults results = csvImportExportService.importCsv(entityId, reader,
                        csvFile.getOriginalFilename(), csvImportCustomizer);
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

    private List<ExportField> getExportFields(Long entityId, List<String> selectedFields) {
        List<FieldDto> fields = entityService.getEntityFields(entityId);
        Map<String, ExportField> exportFieldMap = new LinkedHashMap<>();

        for (FieldDto fieldDto : fields) {
            TypeDto fieldType = fieldDto.getType();
            String type;

            if (fieldType.isRelationship()) {
                type = fieldDto.getMetadata("related.class").getValue();
            } else {
                type = fieldType.getTypeClass();
            }

            exportFieldMap.put(fieldDto.getBasic().getDisplayName(),
                new ExportField(fieldDto.getBasic().getDisplayName(), type, fieldDto.getBasic().getName()));
        }

        if (selectedFields == null || selectedFields.isEmpty()) {
            return new ArrayList<>(exportFieldMap.values());
        }

        List<ExportField> exportFields = new ArrayList<>();

        for (String field : selectedFields) {
            exportFields.add(exportFieldMap.get(field));
        }

        return exportFields;
    }
}
