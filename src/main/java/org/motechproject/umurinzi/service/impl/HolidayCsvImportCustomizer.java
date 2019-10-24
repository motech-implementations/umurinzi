package org.motechproject.umurinzi.service.impl;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.motechproject.mds.service.DefaultCsvImportCustomizer;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.umurinzi.domain.Holiday;
import org.motechproject.umurinzi.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HolidayCsvImportCustomizer extends DefaultCsvImportCustomizer {

    private static final String HOLIDAY_DATE_FORMAT = "yyyy-MM-dd";

    private HolidayService holidayService;

    @Override
    public Object findExistingInstance(Map<String, String> row, MotechDataService motechDataService) {
        String holidayDate = row.get(Holiday.HOLIDAY_DATE_FIELD_NAME);

        if (StringUtils.isBlank(holidayDate)) {
            holidayDate = row.get(Holiday.HOLIDAY_DATE_FIELD_DISPLAY_NAME);
        }

        if (StringUtils.isNotBlank(holidayDate)) {
            LocalDate date = LocalDate.parse(holidayDate, DateTimeFormat.forPattern(HOLIDAY_DATE_FORMAT));
            return holidayService.findByDate(date);
        }

        return null;
    }

    @Override
    public Object doCreate(Object instance, MotechDataService motechDataService) {
        return holidayService.create((Holiday) instance);
    }

    @Override
    public Object doUpdate(Object instance, MotechDataService motechDataService) {
        return holidayService.update((Holiday) instance);
    }

    @Autowired
    public void setHolidayService(HolidayService holidayService) {
        this.holidayService = holidayService;
    }
}
