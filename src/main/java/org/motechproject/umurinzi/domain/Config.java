package org.motechproject.umurinzi.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.domain.enums.EmailSchedulePeriod;

public class Config {

    @Getter
    @Setter
    private Boolean enableZetesImportJob = false;

    @Getter
    @Setter
    private String zetesImportStartTime = UmurinziConstants.ZETES_IMPORT_EVENT_START_TIME;

    @Getter
    @Setter
    private String zetesImportPeriod = UmurinziConstants.ZETES_IMPORT_EVENT_PERIOD;

    @Getter
    @Setter
    private String lastZetesImportDate;

    @Getter
    @Setter
    private String lastSubjectTransferDate;

    @Getter
    @Setter
    private String zetesDbUrl;

    @Getter
    @Setter
    private String zetesDbDriver;

    @Getter
    @Setter
    private String zetesDbUsername;

    @Getter
    @Setter
    private String zetesDbPassword;

    @Getter
    @Setter
    private Boolean enableWelcomeMessage = true;

    @Getter
    @Setter
    private Boolean enableTransferMessage = true;

    @Getter
    @Setter
    private Boolean enableReportJob = false;

    @Getter
    @Setter
    private String reportCalculationStartTime = UmurinziConstants.DAILY_REPORT_EVENT_START_HOUR;

    @Getter
    @Setter
    private String lastCalculationDateForIvrReports;

    @Getter
    @Setter
    private Set<String> ivrAndSmsStatisticReportsToUpdate = new HashSet<>();

    @Getter
    @Setter
    private Boolean sendIvrCalls = false;

    @Getter
    @Setter
    private String ivrSettingsName;

    @Getter
    @Setter
    private String ivrUrl;

    @Getter
    @Setter
    private String apiKey;

    @Getter
    @Setter
    private String statusCallbackUrl;

    @Getter
    @Setter
    private Boolean sendSmsIfVoiceFails = false;

    @Getter
    @Setter
    private Boolean detectVoiceMail = true;

    @Getter
    @Setter
    private Integer retryAttempts;

    @Getter
    @Setter
    private Integer retryDelay;

    @Getter
    @Setter
    private String ivrLanguageId;

    @Getter
    @Setter
    private String voiceSenderId;

    @Getter
    @Setter
    private String smsSenderId;

    @Getter
    @Setter
    private String suspendedMessages;

    @Getter
    @Setter
    private String emailReportHost;

    @Getter
    @Setter
    private Integer emailReportPort;

    @Getter
    @Setter
    private String emailReportAddress;

    @Getter
    @Setter
    private String emailReportPassword;

    @Getter
    @Setter
    private Boolean enableEmailReportJob = false;

    @Getter
    @Setter
    private String emailReportStartDate;

    @Getter
    @Setter
    private EmailSchedulePeriod emailSchedulePeriod;

    @Getter
    @Setter
    private String emailRecipients;

    @Getter
    @Setter
    private String emailSubject;

    @Getter
    @Setter
    private String emailBody;
}
