package org.motechproject.umurinzi.dto;

import lombok.Getter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.motechproject.umurinzi.domain.IvrAndSmsStatisticReport;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.util.serializer.CustomDateTimeSerializer;
import org.motechproject.umurinzi.util.serializer.CustomDoubleSerializer;
import org.motechproject.umurinzi.util.serializer.CustomSubjectSerializer;

@JsonAutoDetect
public class IvrAndSmsStatisticReportDto {

    @JsonProperty
    @JsonSerialize(using = CustomSubjectSerializer.class)
    @Getter
    private Subject subject;

    @JsonProperty
    @Getter
    private String messageId;

    @JsonProperty
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @Getter
    private DateTime sendDate;

    @JsonProperty
    @JsonSerialize(using = CustomDoubleSerializer.class)
    @Getter
    private Double expectedDuration;

    @JsonProperty
    @JsonSerialize(using = CustomDoubleSerializer.class)
    @Getter
    private Double timeListenedTo;

    @JsonProperty
    @JsonSerialize(using = CustomDoubleSerializer.class)
    @Getter
    private Double callLength;

    @JsonProperty
    @JsonSerialize(using = CustomDoubleSerializer.class)
    @Getter
    private Double messagePercentListened;

    @JsonProperty
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @Getter
    private DateTime receivedDate;

    @JsonProperty
    @Getter
    private int numberOfAttempts;

    @JsonProperty
    @Getter
    private String sms;

    @JsonProperty
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @Getter
    private DateTime smsReceivedDate;

    public IvrAndSmsStatisticReportDto(IvrAndSmsStatisticReport ivrAndSmsStatisticReport) {
        subject = ivrAndSmsStatisticReport.getSubject();
        messageId = ivrAndSmsStatisticReport.getMessageId();
        sendDate = ivrAndSmsStatisticReport.getSendDate();
        expectedDuration = ivrAndSmsStatisticReport.getExpectedDuration();
        timeListenedTo = ivrAndSmsStatisticReport.getTimeListenedTo();
        callLength = ivrAndSmsStatisticReport.getCallLength();
        messagePercentListened = ivrAndSmsStatisticReport.getMessagePercentListened();
        receivedDate = ivrAndSmsStatisticReport.getReceivedDate();
        numberOfAttempts = ivrAndSmsStatisticReport.getNumberOfAttempts();
        if (ivrAndSmsStatisticReport.getSmsStatus() != null) {
            sms = ivrAndSmsStatisticReport.getSmsStatus().toString();
        }
        smsReceivedDate = ivrAndSmsStatisticReport.getSmsReceivedDate();
    }
}
