package org.motechproject.umurinzi.scheduler;


import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;

public class UmurinziSchedulerTest {

    @Mock
    private MotechSchedulerService motechSchedulerService;

    private UmurinziScheduler umurinziScheduler;

    @Before
    public void setUp() {
        initMocks(this);
        umurinziScheduler = new UmurinziScheduler(motechSchedulerService);
    }

    @After
    public void cleanup() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldScheduleDailyReportJob() {
        DateTime startDate = DateUtil.newDateTime(LocalDate.now().plusDays(1), Time
            .parseTime(UmurinziConstants.DAILY_REPORT_EVENT_START_HOUR, ":"));

        umurinziScheduler.scheduleDailyReportJob(startDate);

        Period period = Period.days(1);
        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(UmurinziConstants.DAILY_REPORT_EVENT_START_DATE, startDate);
        MotechEvent event = new MotechEvent(UmurinziConstants.DAILY_REPORT_EVENT, eventParameters);
        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);

        verify(motechSchedulerService).safeScheduleRepeatingPeriodJob(job);
    }

    @Test
    public void shouldUnscheduleDailyReportJob() {
        umurinziScheduler.unscheduleDailyReportJob();
        verify(motechSchedulerService).safeUnscheduleAllJobs(UmurinziConstants.DAILY_REPORT_EVENT);
    }
}
