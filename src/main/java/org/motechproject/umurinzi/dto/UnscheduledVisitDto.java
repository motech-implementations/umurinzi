package org.motechproject.umurinzi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.UnscheduledVisit;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;

@NoArgsConstructor
public class UnscheduledVisitDto {

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String participantId;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @Getter
    @Setter
    private String purpose;

    public UnscheduledVisitDto(UnscheduledVisit unscheduledVisit) {
        setId(unscheduledVisit.getId().toString());
        setParticipantId(unscheduledVisit.getSubject().getSubjectId());
        setDate(unscheduledVisit.getDate());
        setPurpose(unscheduledVisit.getPurpose());
    }
}
