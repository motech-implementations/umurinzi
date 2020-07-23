package org.motechproject.umurinzi.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.umurinzi.domain.enums.EnrollmentStatus;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomEnrollmentStatusSerializer;
import org.motechproject.umurinzi.util.serializer.CustomSubjectSerializer;

@Entity(recordHistory = true, name = "ParticipantEnrollments", nonEditable = true, maxFetchDepth = 2)
@NoArgsConstructor
public class SubjectEnrollments {

    public static final String STATUS_PROPERTY_NAME = "status";

    @NonEditable
    @Field(displayName = "Participant")
    @JsonSerialize(using = CustomSubjectSerializer.class)
    @Cascade(persist = false, update = false)
    @Getter
    @Setter
    private Subject subject;

    @Field
    @JsonSerialize(using = CustomEnrollmentStatusSerializer.class)
    @Getter
    @Setter
    private EnrollmentStatus status;

    @NonEditable
    @Field
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate dateOfUnenrollment;

    @NonEditable
    @Field
    @Cascade(delete = true)
    @Getter
    @Setter
    private Set<Enrollment> enrollments = new HashSet<>();

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public SubjectEnrollments(Subject subject) {
        this.subject = subject;
    }

    @Ignore
    public Enrollment findEnrolmentByCampaignName(String campaignName) {
        for (Enrollment enrollment: enrollments) {
            if (campaignName.startsWith(enrollment.getCampaignName())) {
                return enrollment;
            }
        }

        return null;
    }

    @Ignore
    public void addEnrolment(Enrollment enrollment) {
        enrollments.add(enrollment);
    }

    @Ignore
    public void removeEnrolment(Enrollment enrollment) {
        enrollments.remove(enrollment);
    }

    @Override
    public String toString() {
        return status.getValue();
    }
}
