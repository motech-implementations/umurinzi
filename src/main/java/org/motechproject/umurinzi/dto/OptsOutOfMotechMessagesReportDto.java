package org.motechproject.umurinzi.dto;

import lombok.Getter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomSubjectSerializer;

@JsonAutoDetect
public class OptsOutOfMotechMessagesReportDto {

    @JsonProperty
    @JsonSerialize(using = CustomSubjectSerializer.class)
    @Getter
    private Subject subject;

    @JsonProperty
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    private LocalDate dateOfUnenrollment;

    public OptsOutOfMotechMessagesReportDto(SubjectEnrollments subjectEnrollments) {
        subject = subjectEnrollments.getSubject();
        dateOfUnenrollment = subjectEnrollments.getDateOfUnenrollment();
    }
}
