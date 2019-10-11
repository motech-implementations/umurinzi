package org.motechproject.umurinzi.service.impl;

import static org.motechproject.umurinzi.constants.UmurinziConstants.SIMPLE_DATE_FORMATTER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.ivr.domain.CallDetailRecord;
import org.motechproject.ivr.repository.CallDetailRecordDataService;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.domain.IvrAndSmsStatisticReport;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.exception.UmurinziReportException;
import org.motechproject.umurinzi.repository.IvrAndSmsStatisticReportDataService;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.ReportService;
import org.motechproject.umurinzi.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("reportService")
public class ReportServiceImpl implements ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportServiceImpl.class);

    private static final int HUNDRED_PERCENT = 100;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private IvrAndSmsStatisticReportDataService ivrAndSmsStatisticReportDataService;

    @Autowired
    private CallDetailRecordDataService callDetailRecordDataService;

    @Override
    public void generateIvrAndSmsStatisticReports() {
        Config config = configService.getConfig();

        if (StringUtils.isNotBlank(config.getLastCalculationDateForIvrReports())) {
            LocalDate startDate = SIMPLE_DATE_FORMATTER.parseLocalDate(config.getLastCalculationDateForIvrReports());
            generateIvrAndSmsStatisticReportsFromDate(startDate);
        } else {
            generateIvrAndSmsStatisticReportsFromDate(null);
        }

        config = configService.getConfig();
        config.setLastCalculationDateForIvrReports(LocalDate.now().toString(SIMPLE_DATE_FORMATTER));
        configService.updateConfig(config);
    }

    @Override
    public void generateIvrAndSmsStatisticReportsFromDate(LocalDate startDate) {
        List<CallDetailRecord> callDetailRecords = new ArrayList<>();

        if (startDate == null) {
            callDetailRecords = callDetailRecordDataService.findByCallStatus(
                UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_INITIATED);
        } else {
            LocalDate now = DateUtil.now().toLocalDate();

            for (LocalDate date = startDate; date.isBefore(now); date = date.plusDays(1)) {
                String dateString = SIMPLE_DATE_FORMATTER.print(date);
                callDetailRecords.addAll(callDetailRecordDataService.findByMotechTimestampAndCallStatus(dateString, UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_INITIATED));
            }
        }

        Config config = configService.getConfig();
        Set<String> reportsToUpdate = config.getIvrAndSmsStatisticReportsToUpdate();
        config.setIvrAndSmsStatisticReportsToUpdate(null);
        configService.updateConfig(config);

        if (startDate != null && !reportsToUpdate.isEmpty()) {
            callDetailRecords.addAll(callDetailRecordDataService.findByMotechCallIds(reportsToUpdate));
        }

        for (CallDetailRecord callDetailRecord : callDetailRecords) {
            try {
                createIvrAndSmsStatisticReport(callDetailRecord);
                reportsToUpdate.remove(callDetailRecord.getMotechCallId());
            } catch (UmurinziReportException e) {
                LOGGER.warn(e.getMessage());
            }
        }

        config = configService.getConfig();
        reportsToUpdate.addAll(config.getIvrAndSmsStatisticReportsToUpdate());
        config.setIvrAndSmsStatisticReportsToUpdate(reportsToUpdate);
        configService.updateConfig(config);
    }

    private void createIvrAndSmsStatisticReport(CallDetailRecord initialRecord) { //NO CHECKSTYLE CyclomaticComplexity
        DateTimeFormatter motechTimestampFormatter = DateTimeFormat.forPattern(
            UmurinziConstants.IVR_CALL_DETAIL_RECORD_TIME_FORMAT);
        DateTimeFormatter votoTimestampFormatter = DateTimeFormat.forPattern(
            UmurinziConstants.VOTO_TIMESTAMP_FORMAT).withZoneUTC();

        String providerCallId = initialRecord.getProviderCallId();
        Map<String, String> providerExtraData = initialRecord.getProviderExtraData();

        if (StringUtils.isBlank(providerCallId)) {
            throw new UmurinziReportException("Cannot generate report for Call Detail Record with Motech Call Id: %s, because Provider Call Id is empty",
                initialRecord.getMotechCallId());
        }
        if (providerExtraData == null || providerExtraData.isEmpty()) {
            throw new UmurinziReportException("Cannot generate report for Call Detail Record with Motech Call Id: %s, because Provider Extra Data Map is empty",
                initialRecord.getMotechCallId());
        }

        String subjectId = providerExtraData.get(UmurinziConstants.SUBJECT_ID);

        if (StringUtils.isBlank(subjectId)) {
            throw new UmurinziReportException("Cannot generate report for Call Detail Record with Motech Call Id: %s, because no ParticipantId found In Provider Extra Data Map",
                initialRecord.getMotechCallId());
        }

        Subject subject = subjectService.findSubjectBySubjectId(subjectId.trim());

        if (subject == null) {
            throw new UmurinziReportException("Cannot generate report for Call Detail Record with Motech Call Id: %s, because No Participant found with Id: %s",
                initialRecord.getMotechCallId(), subjectId);
        }

        List<CallDetailRecord> callDetailRecords = callDetailRecordDataService.findByExactProviderCallId(providerCallId,
            QueryParams.ascOrder(UmurinziConstants.IVR_CALL_DETAIL_RECORD_MOTECH_TIMESTAMP_FIELD));

        List<CallDetailRecord> endRecords = new ArrayList<>();

        boolean sms = false;
        boolean smsFailed = false;
        String messageId = providerExtraData.get(UmurinziConstants.MESSAGE_ID);
        DateTime sendDate = DateTime.parse(initialRecord.getMotechTimestamp(), motechTimestampFormatter);
        int attempts = 0;
        DateTime receivedDate = null;
        DateTime smsReceivedDate = null;
        double expectedDuration = 0;
        double timeListenedTo = 0;
        double messagePercentListened = 0;

        String smsDeliveryLogId = null;
        CallDetailRecord callRecord = null;
        CallDetailRecord smsRecord = null;

        for (CallDetailRecord callDetailRecord : callDetailRecords) {
            if (callDetailRecord.getCallStatus() == null) {
                continue;
            }

            if (callDetailRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_SUBMITTED)) {
                sms = true;
                smsDeliveryLogId = callDetailRecord.getProviderExtraData().get(UmurinziConstants.IVR_DELIVERY_LOG_ID);
            } else if (callDetailRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_FINISHED)
                || callDetailRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_FAILED)) {
                endRecords.add(callDetailRecord);
            }
        }

        if (endRecords.isEmpty()) {
            throw new UmurinziReportException("Cannot generate report for Call Detail Record with Provider Call Id: %s for Providers with Ids %s, because there is no finished/failed record",
                providerCallId, subjectId);
        }

        if (sms) {
            if (StringUtils.isBlank(smsDeliveryLogId)) {
                throw new UmurinziReportException("Cannot generate report for Call Detail Record with Provider Call Id: %s for Providers with Ids %s, because SMS delivery log is empty",
                    providerCallId, subjectId);
            }

            for (CallDetailRecord callDetailRecord : endRecords) {
                if (smsDeliveryLogId.equals(callDetailRecord.getProviderExtraData().get(UmurinziConstants.IVR_DELIVERY_LOG_ID))) {
                    smsRecord = callDetailRecord;
                } else {
                    callRecord = callDetailRecord;
                }
            }
        } else if (endRecords.size() < 2) {
            callRecord = endRecords.get(0);
        } else {
            LOGGER.warn("SMS was not send for Call Detail Record with Provider Call Id: {} for Providers with Ids {}", providerCallId, subjectId);

            for (CallDetailRecord callDetailRecord : endRecords) {
                if (callDetailRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_FAILED) && smsRecord == null) {
                    smsRecord = callDetailRecord;
                } else {
                    callRecord = callDetailRecord;
                }
            }
        }

        if (smsRecord != null) {
            if (smsRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_FAILED)) {
                smsFailed = true;
            } else {
                String providerTimestamp = smsRecord.getProviderExtraData().get(UmurinziConstants.IVR_CALL_DETAIL_RECORD_END_TIMESTAMP);

                if (StringUtils.isBlank(providerTimestamp)) {
                    throw new UmurinziReportException("Cannot generate report for Call Detail Record with Provider Call Id: %s for Providers with Ids %s, because end_timestamp for SMS Record not found",
                        providerCallId, subjectId);
                }

                smsReceivedDate = DateTime.parse(providerTimestamp, votoTimestampFormatter)
                    .toDateTime(DateTimeZone.getDefault());
            }
        } else if (sms) {
            LOGGER.warn("SMS is sent but not yet received for Call Detail Record with Provider Call Id: {} for Providers with Ids {}", providerCallId, subjectId);

            Config config = configService.getConfig();
            config.getIvrAndSmsStatisticReportsToUpdate().add(initialRecord.getMotechCallId());
            configService.updateConfig(config);
        }

        if (callRecord != null) {
            if (callRecord.getCallStatus().contains(UmurinziConstants.IVR_CALL_DETAIL_RECORD_STATUS_FINISHED)) {
                String providerTimestamp = callRecord.getProviderExtraData().get(UmurinziConstants.IVR_CALL_DETAIL_RECORD_START_TIMESTAMP);

                if (StringUtils.isBlank(providerTimestamp)) {
                    throw new UmurinziReportException("Cannot generate report for Call Detail Record with Provider Call Id: %s for Providers with Ids %s, because start_timestamp for Call Record not found",
                        providerCallId, subjectId);
                }

                receivedDate = DateTime.parse(providerTimestamp, votoTimestampFormatter)
                    .toDateTime(DateTimeZone.getDefault());

                if (StringUtils.isNotBlank(callRecord.getCallDuration())) {
                    timeListenedTo = Double.parseDouble(callRecord.getCallDuration());

                    if (StringUtils.isNotBlank(callRecord.getMessagePercentListened())) {
                        messagePercentListened = Double.parseDouble(callRecord.getMessagePercentListened());
                        String messageSecondsCompleted = callRecord.getProviderExtraData().get(
                            UmurinziConstants.IVR_CALL_DETAIL_RECORD_MESSAGE_SECOND_COMPLETED);

                        if (StringUtils.isNotBlank(messageSecondsCompleted)) {
                            expectedDuration = Double.parseDouble(messageSecondsCompleted) * HUNDRED_PERCENT / messagePercentListened;
                        }
                    }
                }
            }

            String attemptsString = callRecord.getProviderExtraData().get(UmurinziConstants.IVR_CALL_DETAIL_RECORD_NUMBER_OF_ATTEMPTS);
            if (StringUtils.isNotBlank(attemptsString)) {
                attempts = Integer.parseInt(attemptsString);
            }
        }

        IvrAndSmsStatisticReport ivrAndSmsStatisticReport = ivrAndSmsStatisticReportDataService.findByProviderCallIdAndSubjectId(providerCallId, subject.getSubjectId());
        if (ivrAndSmsStatisticReport == null) {
            ivrAndSmsStatisticReport = new IvrAndSmsStatisticReport(providerCallId, subject, messageId, sendDate,
                expectedDuration, timeListenedTo, messagePercentListened, receivedDate, attempts, sms, smsFailed, smsReceivedDate);
            ivrAndSmsStatisticReportDataService.create(ivrAndSmsStatisticReport);
        } else {
            ivrAndSmsStatisticReport.updateReportData(providerCallId, subject, messageId, sendDate, expectedDuration,
                timeListenedTo, messagePercentListened, receivedDate, attempts, sms, smsFailed, smsReceivedDate);
            ivrAndSmsStatisticReportDataService.update(ivrAndSmsStatisticReport);
        }
    }
}
