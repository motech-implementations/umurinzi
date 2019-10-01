package org.motechproject.umurinzi.importer;

import java.util.Map;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.scheduler.UmurinziScheduler;
import org.motechproject.scheduler.service.MotechSchedulerService;
import org.springframework.stereotype.Component;

@Component
public class JobImporter implements OsgiServiceLifecycleListener {

    private MotechSchedulerService motechSchedulerService;

    @Override
    public void bind(Object o, Map map) throws Exception {
        this.motechSchedulerService = (MotechSchedulerService) o;
        importDailyReportJob();
    }

    @Override
    public void unbind(Object o, Map map) throws Exception {
        this.motechSchedulerService = null;
    }

    private void importDailyReportJob() {
        DateTime startDate =  DateUtil.newDateTime(LocalDate.now().plusDays(1),
            Time.parseTime(UmurinziConstants.DAILY_REPORT_EVENT_START_HOUR, ":"));

        UmurinziScheduler umurinziScheduler = new UmurinziScheduler(motechSchedulerService);
        umurinziScheduler.scheduleDailyReportJob(startDate);
    }
}
