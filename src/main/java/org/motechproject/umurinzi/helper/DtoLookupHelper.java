package org.motechproject.umurinzi.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.commons.api.Range;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.domain.UnscheduledVisit;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.enums.DateFilter;
import org.motechproject.umurinzi.domain.enums.EnrollmentStatus;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.dto.LookupFieldDto;
import org.motechproject.mds.dto.SettingDto;

public final class DtoLookupHelper {

    private static final String NOT_BLANK_REGEX = ".";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<VisitType> AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN =
        new HashSet<>(Arrays.asList(VisitType.BOOST_VACCINATION_DAY, VisitType.D1_VISIT,
            VisitType.D3_VISIT, VisitType.D28_VISIT, VisitType.D7_VISIT, VisitType.D57_VISIT,
            VisitType.D59_VISIT, VisitType.D63_VISIT, VisitType.D77_VISIT, VisitType.D84_VISIT,
            VisitType.D180_VISIT, VisitType.D365_VISIT, VisitType.D720_VISIT));

    private DtoLookupHelper() {
    }

    public static GridSettings changeLookupForUnscheduled(GridSettings settings) throws IOException {
        Map<String, Object> fieldsMap = new HashMap<>();
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter != null) {

            if (StringUtils.isBlank(settings.getFields())) {
                settings.setFields("{}");
            }

            String lookup = settings.getLookup();
            if (StringUtils.isBlank(lookup)) {
                settings.setLookup("Find By Date");
            } else {
                fieldsMap = getFields(settings.getFields());
                settings.setLookup(lookup + " And Date");
            }

            Map<String, String> rangeMap = getDateRangeFromFilter(settings);

            fieldsMap.put(UnscheduledVisit.DATE_PROPERTY_NAME, rangeMap);
            settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        }
        return settings;
    }

    //CHECKSTYLE:OFF: checkstyle:cyclomaticcomplexity
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public static GridSettings changeLookupForVisitReschedule(GridSettings settings) throws IOException {  //NO CHECKSTYLE CyclomaticComplexity
        Map<String, Object> fieldsMap = new HashMap<>();

        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }

        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Visit Type Set And Planned Date");
            fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
        } else {
            fieldsMap = getFields(settings.getFields());
        }

        Map<String, String> rangeMap = getDateRangeFromFilter(settings);
        String lookup = settings.getLookup();

        switch (lookup) {
            case "Find By Visit Type And Planned Date":
                break;
            case "Find By Visit Planned Date":
                fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                settings.setLookup(lookup + " Range And Visit Type Set");
                break;
            case "Find By Visit Type Set And Planned Date":
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                }
                break;
            case "Find By Visit Type And Actual Date":
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " Range And Planned Date Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                }
                break;
            default:
                if (rangeMap != null && (StringUtils.isNotBlank(rangeMap.get("min")) || StringUtils.isNotBlank(rangeMap.get("max")))) {
                    settings.setLookup(lookup + " And Visit Type Set And Planned Date Range");
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, rangeMap);
                    fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                } else {
                    fieldsMap.put(Visit.VISIT_TYPE_PROPERTY_NAME, AVAILABLE_VISIT_TYPES_FOR_RESCHEDULE_SCREEN);
                    settings.setLookup(lookup + " And Visit Type Set");
                }
                break;
        }
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }
    //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity

    public static LookupDto changeVisitTypeLookupOptionsOrder(LookupDto lookupDto) {
        if (lookupDto.getLookupFields() != null) {
            for (LookupFieldDto lookupFieldDto: lookupDto.getLookupFields()) {
                if (Visit.VISIT_TYPE_DISPLAY_NAME.equals(lookupFieldDto.getDisplayName())
                        || Visit.VISIT_TYPE_DISPLAY_NAME.equals(lookupFieldDto.getRelatedFieldDisplayName())) {
                    for (SettingDto settingDto : lookupFieldDto.getSettings()) {
                        if ("mds.form.label.values".equals(settingDto.getName())) {
                            List<String> visitTypes = new ArrayList<>();
                            for (VisitType visitType: VisitType.values()) {
                                visitTypes.add(visitType.toString() + ": " + visitType.getDisplayValue());
                            }
                            settingDto.setValue(visitTypes);
                        }
                    }
                }
            }
        }

        return lookupDto;
    }

    public static GridSettings changeLookupAndOrderForFollowupsMissedClinicVisitsReport(GridSettings settings) throws IOException { //NO CHECKSTYLE CyclomaticComplexity
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, String> fieldsMap = new HashMap<>();
        String newLookupName;

        changeOrderForFollowupsMissedClinicVisitsReport(settings);
        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }
        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Planned Date Less And Actual Date Eq And Subject Phone Number Eq");
            fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, LocalDate.now().toString(formatter));
            fieldsMap.put(Visit.VISIT_DATE_PROPERTY_NAME, null);
            settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        } else {
            fieldsMap = getFieldsMap(settings.getFields());

            switch (settings.getLookup()) {
                case "Find By Planned Visit Date":
                case "Find By Planned Visit Date And Type":
                    LocalDate date = getLocalDateFromLookupFields(settings.getFields(),
                        Visit.VISIT_PLANNED_DATE_PROPERTY_NAME);

                    LocalDate maxDate = LocalDate.now().minusDays(1);
                    if (date == null || date.isAfter(maxDate)) {
                        return null;
                    }
                    fieldsMap.put(Visit.VISIT_DATE_PROPERTY_NAME, null);

                    newLookupName = settings.getLookup() + " Eq";
                    break;
                case "Find By Planned Visit Date Range":
                case "Find By Planned Visit Date Range And Type":
                    Range<LocalDate> dateRange = getDateRangeFromLookupFields(settings.getFields(),
                        Visit.VISIT_PLANNED_DATE_PROPERTY_NAME);
                    if (!checkAndUpdateDateRangeForFollowupsMissedClinicVisitsReport(dateRange, settings)) {
                        return null;
                    }
                    fieldsMap.put(Visit.VISIT_DATE_PROPERTY_NAME, null);

                    newLookupName = settings.getLookup() + " Eq";
                    break;
                default:
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, LocalDate.now().toString(formatter));
                    fieldsMap.put(Visit.VISIT_DATE_PROPERTY_NAME, null);

                    newLookupName = settings.getLookup() + " Less";
                    break;
            }
            settings.setLookup(newLookupName);
        }
        fieldsMap.put(Visit.SUBJECT_PHONE_NUMBER_PROPERTY_NAME, null);
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }

    public static GridSettings changeLookupAndOrderForMandEMissedClinicVisitsReport(GridSettings settings) throws IOException { //NO CHECKSTYLE CyclomaticComplexity
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, String> fieldsMap = new HashMap<>();
        String newLookupName;

        changeOrderForFollowupsMissedClinicVisitsReport(settings);
        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }
        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Planned Visit Date Less And Actual Visit Date");
            fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, LocalDate.now().toString(formatter));
        } else {
            fieldsMap = getFieldsMap(settings.getFields());

            switch (settings.getLookup()) {
                case "Find By Planned Visit Date":
                case "Find By Planned Visit Date And Type":
                    LocalDate date = getLocalDateFromLookupFields(settings.getFields(),
                        Visit.VISIT_PLANNED_DATE_PROPERTY_NAME);

                    LocalDate maxDate = LocalDate.now().minusDays(1);
                    if (date == null || date.isAfter(maxDate)) {
                        return null;
                    }

                    newLookupName = settings.getLookup() + " And Actual Visit Date";
                    break;
                case "Find By Planned Visit Date Range":
                case "Find By Planned Visit Date Range And Type":
                    Range<LocalDate> dateRange = getDateRangeFromLookupFields(settings.getFields(),
                        Visit.VISIT_PLANNED_DATE_PROPERTY_NAME);
                    if (!checkAndUpdateDateRangeForFollowupsMissedClinicVisitsReport(dateRange, settings)) {
                        return null;
                    }

                    newLookupName = settings.getLookup() + " And Actual Visit Date";
                    break;
                default:
                    fieldsMap.put(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, LocalDate.now().toString(formatter));

                    newLookupName = settings.getLookup() + " And Planned Visit Date And Actual Visit Date";
                    break;
            }
            settings.setLookup(newLookupName);
        }

        fieldsMap.put(Visit.VISIT_DATE_PROPERTY_NAME, null);
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }

    public static GridSettings changeLookupAndOrderForOptsOutOfMotechMessagesReport(GridSettings settings) throws IOException {
        Map<String, Object> fieldsMap = new HashMap<>();

        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }

        if (StringUtils.isBlank(settings.getLookup())) {
            settings.setLookup("Find By Status");
        } else {
            fieldsMap = getFields(settings.getFields());

            String newLookupName = settings.getLookup() + " And Status";
            settings.setLookup(newLookupName);
        }

        fieldsMap.put(SubjectEnrollments.STATUS_PROPERTY_NAME, EnrollmentStatus.UNENROLLED.toString());
        settings.setFields(OBJECT_MAPPER.writeValueAsString(fieldsMap));
        return settings;
    }

    private static boolean checkAndUpdateDateRangeForFollowupsMissedClinicVisitsReport(Range<LocalDate> dateRange, GridSettings settings) {
        if (dateRange == null) {
            return false;
        }
        LocalDate maxDate = LocalDate.now().minusDays(1);
        if (dateRange.getMin() != null && dateRange.getMin().isAfter(maxDate)) {
            return false;
        } else if (dateRange.getMax() == null || dateRange.getMax().isAfter(maxDate)) {
            settings.setFields(setNewMaxDateInRangeFields(settings.getFields(), Visit.VISIT_PLANNED_DATE_PROPERTY_NAME, maxDate));
        }
        return true;
    }

    private static void changeOrderForFollowupsMissedClinicVisitsReport(GridSettings settings)
    {
        if (StringUtils.isNotBlank(settings.getSortColumn())) {
            String sortColumn = settings.getSortColumn();
            if ("planedVisitDate".equals(sortColumn) || "noOfDaysExceededVisit".equals(sortColumn)) {
                settings.setSortColumn(Visit.VISIT_PLANNED_DATE_PROPERTY_NAME);
                if ("noOfDaysExceededVisit".equals(sortColumn)) {
                    if ("asc".equals(settings.getSortDirection())) {
                        settings.setSortDirection("desc");
                    } else {
                        settings.setSortDirection("asc");
                    }
                }
            }
        }
        if (StringUtils.isBlank(settings.getFields())) {
            settings.setFields("{}");
        }
    }

    private static Object getObjectFromLookupFields(String lookupFields, String fieldName) {
        Map<String, Object> fieldsMap;
        try {
            fieldsMap = getFields(lookupFields);
        } catch (IOException e) {
            throw new UmurinziLookupException("Invalid lookup params", e);
        }
        return fieldsMap.get(fieldName);
    }

    private static LocalDate getLocalDateFromLookupFields(String lookupFields, String dateName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        return formatter.parseLocalDate((String) getObjectFromLookupFields(lookupFields, dateName));
    }

    private static Range<LocalDate> getDateRangeFromLookupFields(String lookupFields, String dateName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        LocalDate min = null;
        LocalDate max = null;

        @SuppressWarnings("unchecked")
        Map<String, String> rangeMap = (Map<String, String>) getObjectFromLookupFields(lookupFields, dateName);
        if (StringUtils.isNotBlank(rangeMap.get("min"))) {
            min = formatter.parseLocalDate(rangeMap.get("min"));
        }
        if (StringUtils.isNotBlank(rangeMap.get("max"))) {
            max = formatter.parseLocalDate(rangeMap.get("max"));
        }
        if (max != null && min != null && max.isBefore(min)) {
            return null;
        }
        return new Range<>(min, max);
    }

    private static String setNewMaxDateInRangeFields(String lookupFields, String dateName, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
        Map<String, Object> fieldsMap;

        try {
            fieldsMap = getFields(lookupFields);
        } catch (IOException e) {
            throw new UmurinziLookupException("Invalid lookup params", e);
        }

        @SuppressWarnings("unchecked")
        Map<String, String> rangeMap = (Map<String, String>) fieldsMap.get(dateName);
        rangeMap.remove("max");
        rangeMap.put("max", date.toString(formatter));

        try {
            return OBJECT_MAPPER.writeValueAsString(fieldsMap);
        } catch (IOException e) {
            throw new UmurinziLookupException("Invalid lookup params", e);
        }
    }

    private static Map<String, String> getDateRangeFromFilter(GridSettings settings) {
        DateFilter dateFilter = settings.getDateFilter();

        if (dateFilter == null) {
            return null;
        }

        Map<String, String> rangeMap = new HashMap<>();

        if (DateFilter.DATE_RANGE.equals(dateFilter)) {
            rangeMap.put("min", settings.getStartDate());
            rangeMap.put("max", settings.getEndDate());
        } else {
            Range<LocalDate> dateRange = dateFilter.getRange();
            rangeMap.put("min", dateRange.getMin().toString(UmurinziConstants.SIMPLE_DATE_FORMAT));
            rangeMap.put("max", dateRange.getMax().toString(UmurinziConstants.SIMPLE_DATE_FORMAT));
        }

        return rangeMap;
    }

    private static Map<String, Object> getFields(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }

    private static Map<String, String> getFieldsMap(String lookupFields) throws IOException {
        return OBJECT_MAPPER.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }
}
