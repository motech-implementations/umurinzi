package org.motechproject.umurinzi.helper;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.motechproject.umurinzi.domain.enums.DateFilter;
import org.motechproject.umurinzi.web.domain.GridSettings;

public class DtoLookupHelperTest {

    private static final String START_DATE = "2017-01-01";
    private static final String END_DATE = "2017-01-20";
    private static final String LOOKUP = "Find By Parameter";
    private static final String ACTUAL_DATE = "date";
    private static final String MIN = "min";
    private static final String MAX = "max";
    private static final String PARAM_KEY = "param";
    private static final String PARAM_VALUE = "val";
    private static final String OPEN_BRACKET = "{";
    private static final String CLOSE_BRACKET = "}";
    private static final String PARAM_FIELD = "\"%s\":\"%s\"";

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldChangeLookupToFindByDate() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForUnscheduled(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);

        assertEquals("Find By Date", returnedSettings.getLookup());
        assertEquals(1, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
    }

    @Test
    public void shouldAddDateToLookup() throws IOException {
        GridSettings settings = new GridSettings();
        settings.setDateFilter(DateFilter.DATE_RANGE);
        settings.setStartDate(START_DATE);
        settings.setEndDate(END_DATE);
        settings.setLookup(LOOKUP);
        settings.setFields(OPEN_BRACKET + String.format(PARAM_FIELD, PARAM_KEY, PARAM_VALUE) + CLOSE_BRACKET);

        GridSettings returnedSettings = DtoLookupHelper.changeLookupForUnscheduled(settings);

        Map<String, Object> returnedFields = objectMapper.readValue(returnedSettings.getFields(), new TypeReference<HashMap>() { });
        String returnedMinDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MIN);
        String returnedMaxDate = (String) ((LinkedHashMap) returnedFields.get(ACTUAL_DATE)).get(MAX);
        String paramValue = (String) returnedFields.get(PARAM_KEY);

        assertEquals(LOOKUP + " And Date", returnedSettings.getLookup());
        assertEquals(2, returnedFields.keySet().size());
        assertEquals(START_DATE, returnedMinDate);
        assertEquals(END_DATE, returnedMaxDate);
        assertEquals(PARAM_VALUE, paramValue);
    }
}
