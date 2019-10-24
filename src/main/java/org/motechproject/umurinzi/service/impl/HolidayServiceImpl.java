package org.motechproject.umurinzi.service.impl;

import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Holiday;
import org.motechproject.umurinzi.repository.HolidayDataService;
import org.motechproject.umurinzi.service.HolidayService;
import org.motechproject.umurinzi.service.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("holidayService")
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayDataService holidayDataService;

    @Autowired
    private VisitService visitService;

    @Override
    public Holiday findByDate(LocalDate date) {
        return holidayDataService.findByHolidayDate(date);
    }

    @Override
    public Holiday create(Holiday holiday) {
        dataChanged(holiday);
        return holidayDataService.create(holiday);
    }

    @Override
    public Holiday update(Holiday holiday) {
        dataChanged(holiday);
        return holidayDataService.update(holiday);
    }

    @Override
    public void dataChanged(Holiday holiday) {
        visitService.recalculateVisitsForHoliday(holiday.getHolidayDate());
    }
}
