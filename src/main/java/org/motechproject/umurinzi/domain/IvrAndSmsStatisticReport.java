package org.motechproject.umurinzi.domain;

import javax.jdo.annotations.Unique;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.motechproject.umurinzi.domain.enums.SmsStatus;
import org.motechproject.mds.annotations.Access;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.util.SecurityMode;

@Access(value = SecurityMode.PERMISSIONS, members = { "manageUmurinzi" })
@Entity(nonEditable = true, maxFetchDepth = 3)
@Unique(name = "providerCallIdAndSubject", members = {"providerCallId", "subject" })
public class IvrAndSmsStatisticReport {

    @Field
    @Getter
    @Setter
    private String providerCallId;

    @Field(displayName = "Participant")
    @Getter
    @Setter
    private Subject subject;

    @Field
    @Getter
    @Setter
    private String messageId;

    @Field(displayName = "Sent Date")
    @Getter
    @Setter
    private DateTime sendDate;

    @Field
    @Getter
    @Setter
    private double expectedDuration;

    @Field
    @Getter
    @Setter
    private double timeListenedTo;

    @Field
    @Getter
    @Setter
    private double messagePercentListened;

    @Field
    @Getter
    @Setter
    private DateTime receivedDate;

    @Field
    @Getter
    @Setter
    private int numberOfAttempts;

    @Field
    @Getter
    @Setter
    private SmsStatus smsStatus;

    @Field
    @Getter
    @Setter
    private DateTime smsReceivedDate;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public IvrAndSmsStatisticReport(String providerCallId, Subject subject, String messageId, DateTime sendDate, double expectedDuration, double timeListenedTo, //NO CHECKSTYLE ParameterNumber
                                    double messagePercentListened, DateTime receivedDate, int numberOfAttempts, boolean sms, boolean smsFailed, DateTime smsReceivedDate) {
        this.providerCallId = providerCallId;
        this.subject = subject;
        this.messageId = messageId;
        this.sendDate = sendDate;
        this.expectedDuration = expectedDuration;
        this.timeListenedTo = timeListenedTo;
        this.messagePercentListened = messagePercentListened;
        this.receivedDate = receivedDate;
        this.numberOfAttempts = numberOfAttempts;
        this.smsStatus = calculateSmsStatus(sms, smsFailed);
        this.smsReceivedDate = smsReceivedDate;
    }

    public void updateReportData(String providerCallId, Subject subject, String messageId, DateTime sendDate, double expectedDuration, double timeListenedTo, //NO CHECKSTYLE ParameterNumber
                                 double messagePercentListened, DateTime receivedDate, int numberOfAttempts, boolean sms, boolean smsFailed, DateTime smsReceivedDate) {
        this.providerCallId = providerCallId;
        this.subject = subject;
        this.messageId = messageId;
        this.sendDate = sendDate;
        this.expectedDuration = expectedDuration;
        this.timeListenedTo = timeListenedTo;
        this.messagePercentListened = messagePercentListened;
        this.receivedDate = receivedDate;
        this.numberOfAttempts = numberOfAttempts;
        this.smsStatus = calculateSmsStatus(sms, smsFailed);
        this.smsReceivedDate = smsReceivedDate;
    }

    private SmsStatus calculateSmsStatus(boolean sms, boolean smsFailed) {
        if (smsFailed) {
            return SmsStatus.FAIL;
        }
        if (sms) {
            return SmsStatus.YES;
        }
        return SmsStatus.NO;
    }
}
