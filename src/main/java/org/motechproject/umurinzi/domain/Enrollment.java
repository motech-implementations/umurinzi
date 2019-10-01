package org.motechproject.umurinzi.domain;

import javax.jdo.annotations.Unique;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.commons.date.model.Time;
import org.motechproject.umurinzi.domain.enums.EnrollmentStatus;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomEnrollmentStatusSerializer;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.messagecampaign.domain.campaign.CampaignEnrollment;

@Entity(nonEditable = true, maxFetchDepth = 1)
@Unique(name = "externalIdAndCampaignName", members = {"externalId", "campaignName" })
@NoArgsConstructor
public class Enrollment {

    @Field(required = true)
    @Getter
    private String externalId;

    @Field(required = true)
    @Getter
    private String campaignName;

    @Field(required = true)
    @JsonSerialize(using = CustomEnrollmentStatusSerializer.class)
    @Getter
    private EnrollmentStatus status;

    @Field
    @JsonSerialize(using = CustomEnrollmentStatusSerializer.class)
    @Getter
    @Setter
    private EnrollmentStatus previousStatus;

    @Field(required = true)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate referenceDate;

    @Field
    @Getter
    @Setter
    private Time deliverTime;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public Enrollment(String externalId, String campaignName, LocalDate referenceDate) {
        this.externalId = externalId;
        this.campaignName = campaignName;
        this.referenceDate = referenceDate;
        this.status = EnrollmentStatus.ENROLLED;
    }

    public Enrollment(String externalId, String campaignName, LocalDate referenceDate, Time deliverTime) {
        this(externalId, campaignName, referenceDate);
        this.deliverTime = deliverTime;
    }

    public Enrollment(String externalId, String campaignName, LocalDate referenceDate, EnrollmentStatus status) {
        this(externalId, campaignName, referenceDate);
        this.status = status;
    }

    public void setStatus(EnrollmentStatus status) {
        if (status != null && !status.equals(this.status)) {
            previousStatus = this.status;
        }
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enrollment)) {
            return false;
        }

        Enrollment that = (Enrollment) o;

        return externalId.equals(that.externalId) && campaignName.equals(that.campaignName);

    }

    @Override
    public int hashCode() {
        int result = externalId.hashCode();
        result = 31 * result + campaignName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return externalId + " - " + campaignName + " - " + status;
    }

    public CampaignEnrollment toCampaignEnrollment() {
        CampaignEnrollment enrollment = new CampaignEnrollment(externalId, campaignName);
        enrollment.setDeliverTime(deliverTime);
        enrollment.setReferenceDate(referenceDate);

        return enrollment;
    }
}
