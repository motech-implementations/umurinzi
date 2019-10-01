package org.motechproject.umurinzi.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.motechproject.umurinzi.constants.UmurinziConstants;

public class Config {

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
    private Boolean sendIvrCalls = true;

    @Getter
    @Setter
    private String ivrSettingsName;

    @Getter
    @Setter
    private String apiKey;

    @Getter
    @Setter
    private String statusCallbackUrl;

    @Getter
    @Setter
    private Boolean sendSmsIfVoiceFails = true;

    @Getter
    @Setter
    private Boolean detectVoiceMail = true;

    @Getter
    @Setter
    private Integer retryAttempts;

    @Getter
    @Setter
    private Integer retryDelay;
}
