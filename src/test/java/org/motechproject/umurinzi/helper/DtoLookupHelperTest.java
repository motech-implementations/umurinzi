package org.motechproject.umurinzi.helper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.motechproject.umurinzi.domain.enums.DateFilter;
import org.motechproject.umurinzi.web.domain.GridSettings;

public class DtoLookupHelperTest {

    private static final String START_DATE = "2017-01-01";
    private static final String END_DATE = "2017-01-20";
    private static final String START_DATE_2 = "2017-02-01";
    private static final String END_DATE_2 = "2017-02-20";
    private static final String DEFAULT_VISIT_RESCHEDULE_LOOKUP = "Find By Visit Type Set And Planned Date Range";
    private static final String BOOST_VAC_DAY = "BOOST_VACCINATION_DAY";
    private static final String ACTUAL_DATE = "date";
    private static final String PLANNED_DATE = "dateProjected";
    private static final String SUBJECT_ID_FIELD = "subject.subjectId";
    private static final String SUBJECT_ID_VAL = "subjectId";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String TYPE = "type";
    private static final String OPEN_BRACKET = "{";
    private static final String CLOSE_BRACKET = "}";
    private static final String PARAM_FIELD = "\"%s\":\"%s\"";
    private static final String DATE_PARAM_FIELD = "\"%s\":{\"min\":\"%s\",\"max\":\"%s\"}";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldReturnVisitRescheduleDefaultLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals(DEFAULT_VISIT_RESCHEDULE_LOOKUP, returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndPlannedDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(null);
        settings.setEndDate(null);
        settings.setLookup("Find By Visit Type And Planned Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, PLANNED_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Planned Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }

    @Test
    public void testVisitRescheduleFindByPlannedDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(START_DATE_2);
        settings.setLookup("Find By Visit Planned Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, PLANNED_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Planned Date Range And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndActualDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Visit Type And Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Actual Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }

    @Test
    public void testVisitRescheduleFindByVisitActualDateLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(null);
        settings.setEndDate(null);
        settings.setLookup("Find By Visit Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Actual Date And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantIdLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setLookup("Find By Participant Id");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_ID_FIELD, SUBJECT_ID_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String subjectId = (String) returnedFields.get(SUBJECT_ID_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Participant Id And Visit Type Set", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(SUBJECT_ID_VAL, subjectId);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByParticipantIdLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup("Find By Participant Id");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, SUBJECT_ID_FIELD, SUBJECT_ID_VAL) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String subjectId = (String) returnedFields.get(SUBJECT_ID_FIELD);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);

        assertEquals("Find By Participant Id And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(SUBJECT_ID_VAL, subjectId);
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitActualDateLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(END_DATE_2);
        settings.setLookup("Find By Visit Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedMinPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        List<String> returnedVisitTypes = (List<String>) (returnedFields.get(TYPE));

        assertEquals("Find By Visit Actual Date And Visit Type Set And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinActualDate);
        assertEquals(END_DATE, returnedMaxActualDate);
        assertEquals(START_DATE_2, returnedMinPlannedDate);
        assertEquals(END_DATE_2, returnedMaxPlannedDate);
        assertEquals(1, returnedVisitTypes.size());
    }

    @Test
    public void testVisitRescheduleFindByVisitTypeAndActualDateLookupRange() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE_2);
        settings.setEndDate(END_DATE_2);
        settings.setLookup("Find By Visit Type And Actual Date");
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, TYPE, BOOST_VAC_DAY) + "," +
                String.format(DATE_PARAM_FIELD, ACTUAL_DATE, START_DATE, END_DATE) + CLOSE_BRACKET
        );

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForVisitReschedule(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxActualDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String returnedMinPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MIN);
        String returnedMaxPlannedDate = (String) ((LinkedHashMap) returnedFields.get(PLANNED_DATE)).get(MAX);
        String returnedVisitType = (String) returnedFields.get(TYPE);

        assertEquals("Find By Visit Type And Actual Date Range And Planned Date Range", returnedSettings.getLookup());
        assertEquals(3, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinActualDate);
        assertEquals(END_DATE, returnedMaxActualDate);
        assertEquals(START_DATE_2, returnedMinPlannedDate);
        assertEquals(END_DATE_2, returnedMaxPlannedDate);
        assertEquals(BOOST_VAC_DAY, returnedVisitType);
    }
}
