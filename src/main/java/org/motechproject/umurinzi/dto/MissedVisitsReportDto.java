package org.motechproject.umurinzi.dto;

import lombok.Getter;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomSubjectSerializer;
import org.motechproject.umurinzi.util.serializer.CustomVisitTypeSerializer;

@JsonAutoDetect
public class MissedVisitsReportDto {

    @JsonProperty
    @Getter
    private int noOfDaysExceededVisit;

    @JsonProperty
    @JsonSerialize(using = CustomSubjectSerializer.class)
    @Getter
    private Subject subject;

    @JsonProperty
    @JsonSerialize(using = CustomVisitTypeSerializer.class)
    @Getter
    private VisitType type;

    @JsonProperty
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    private LocalDate dateProjected;

    public MissedVisitsReportDto(Visit entityObject) {
        dateProjected = entityObject.getDateProjected();
        if (dateProjected == null) {
            noOfDaysExceededVisit = 0;
        } else {
            noOfDaysExceededVisit = Days.daysBetween(dateProjected, LocalDate.now()).getDays();
        }

        subject = entityObject.getSubject();
        type = entityObject.getType();
        dateProjected = entityObject.getDateProjected();
    }

    @JsonSerialize(using = CustomDateSerializer.class)
    public LocalDate getPlanedVisitDate() {
        return dateProjected;
    }
}
