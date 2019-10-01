package org.motechproject.umurinzi.scheduler;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.exception.UmurinziInitiateCallException;
import org.motechproject.umurinzi.helper.IvrCallHelper;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.motechproject.umurinzi.service.ReportService;
import org.motechproject.messagecampaign.EventKeys;
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

    @MotechListener(subjects = { UmurinziConstants.DAILY_REPORT_EVENT })
    public void generateDailyReport(MotechEvent event) {
        LOGGER.info("Started generation of daily reports...");
        reportService.generateIvrAndSmsStatisticReports();
        LOGGER.info("Daily Reports generation completed");
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
}
