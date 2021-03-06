package org.motechproject.umurinzi.scheduler;

import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.event.MotechEvent;
import org.motechproject.scheduler.contract.RepeatingPeriodSchedulableJob;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UmurinziScheduler {
    private MotechSchedulerService motechSchedulerService;

    @Autowired
    public UmurinziScheduler(MotechSchedulerService motechSchedulerService) {
        this.motechSchedulerService = motechSchedulerService;
    }

    public void scheduleDailyReportJob(DateTime startDate) {
        Period period = Period.days(1);

        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(UmurinziConstants.DAILY_REPORT_EVENT_START_DATE, startDate);

        MotechEvent event = new MotechEvent(UmurinziConstants.DAILY_REPORT_EVENT, eventParameters);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleDailyReportJob() {
        motechSchedulerService.safeUnscheduleAllJobs(UmurinziConstants.DAILY_REPORT_EVENT);
    }

    public void scheduleZetesImportJob(DateTime startDate, Period period) {
        Map<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(UmurinziConstants.ZETES_IMPORT_EVENT_START_DATE, startDate);

        MotechEvent event = new MotechEvent(UmurinziConstants.ZETES_IMPORT_EVENT, eventParameters);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleZetesImportJob() {
        motechSchedulerService.safeUnscheduleAllJobs(UmurinziConstants.ZETES_IMPORT_EVENT);
    }

    public void scheduleClearExportTasksJob() {
        DateTime startDate =  DateUtil.newDateTime(LocalDate.now().plusDays(1),
            Time.parseTime(UmurinziConstants.CLEAR_EXPORT_TASKS_EVENT_START_TIME, ":"));
        Period period = Period.days(1);

        MotechEvent event = new MotechEvent(UmurinziConstants.CLEAR_EXPORT_TASKS_EVENT);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void scheduleEmailReportJob(DateTime startDate, Period period) {
        MotechEvent event = new MotechEvent(UmurinziConstants.SEND_EMAIL_REPORT_EVENT);

        RepeatingPeriodSchedulableJob job = new RepeatingPeriodSchedulableJob(event, startDate.toDate(), null, period, true);
        motechSchedulerService.safeScheduleRepeatingPeriodJob(job);
    }

    public void unscheduleEmailReportJob() {
        motechSchedulerService.safeUnscheduleAllJobs(UmurinziConstants.SEND_EMAIL_REPORT_EVENT);
    }

    public void rescheduleEmailReportJob(DateTime startDate, Period period) {
        unscheduleEmailReportJob();
        scheduleEmailReportJob(startDate, period);
    }
}
