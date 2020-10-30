package org.motechproject.umurinzi.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.messagecampaign.EventKeys;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.exception.UmurinziInitiateCallException;
import org.motechproject.umurinzi.helper.IvrCallHelper;
import org.motechproject.umurinzi.helper.ZetesHelper;
import org.motechproject.umurinzi.service.ExportService;
import org.motechproject.umurinzi.service.JasperReportsService;
import org.motechproject.umurinzi.service.ReportService;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UmurinziEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UmurinziEventListener.class);

    @Autowired
    private ReportService reportService;

    @Autowired
    private UmurinziEnrollmentService umurinziEnrollmentService;

    @Autowired
    private IvrCallHelper ivrCallHelper;

    @Autowired
    private ZetesHelper zetesHelper;

    @Autowired
    private ExportService exportService;

    @Autowired
    private JasperReportsService jasperReportsService;

    @MotechListener(subjects = { UmurinziConstants.DAILY_REPORT_EVENT })
    public void generateDailyReport(MotechEvent event) {
        LOGGER.info("Started generation of daily reports...");
        reportService.generateIvrAndSmsStatisticReports();
        LOGGER.info("Daily Reports generation completed");
    }

    @MotechListener(subjects = { UmurinziConstants.ZETES_IMPORT_EVENT })
    public void importZetesData(MotechEvent event) {
        LOGGER.info("Started import of Zetes data...");
        zetesHelper.fetchZetesData();
//        zetesHelper.transferSubjects();
        LOGGER.info("Zetes data import completed");
    }

    @MotechListener(subjects = EventKeys.CAMPAIGN_COMPLETED)
    public void completeCampaign(MotechEvent event) {
        String campaignName = (String) event.getParameters().get(EventKeys.CAMPAIGN_NAME_KEY);
        String externalId = (String) event.getParameters().get(EventKeys.EXTERNAL_ID_KEY);

        umurinziEnrollmentService.completeCampaign(externalId, campaignName);
    }

    @MotechListener(subjects = EventKeys.SEND_MESSAGE)
    public void initiateIvrCall(MotechEvent event) {
        LOGGER.debug("Handling Motech event {}: {}", event.getSubject(), event.getParameters().toString());

        String messageKey = (String) event.getParameters().get(EventKeys.MESSAGE_KEY);
        String externalId = (String) event.getParameters().get(EventKeys.EXTERNAL_ID_KEY);

        try {
            ivrCallHelper.initiateIvrCall(messageKey, externalId);
        } catch (UmurinziInitiateCallException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @MotechListener(subjects = { UmurinziConstants.CLEAR_EXPORT_TASKS_EVENT })
    public void clearExportTasks(MotechEvent event) {
        exportService.cancelAllExportTasks();
    }

    @MotechListener(subjects = { UmurinziConstants.SEND_EMAIL_REPORT_EVENT })
    public void sendEmailReport(MotechEvent event) {
        LOGGER.info("Sending email report...");
        jasperReportsService.sendVaccinationSummaryReport();
        LOGGER.info("Email report sent");
    }
}
