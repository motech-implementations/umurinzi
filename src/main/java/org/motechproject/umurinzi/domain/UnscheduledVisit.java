package org.motechproject.umurinzi.domain;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;

@Entity(recordHistory = true)
public class UnscheduledVisit {

    public static final String DATE_PROPERTY_NAME = "date";

    @Field
    @Getter
    @Setter
    private Long id;

    @Field(required = true, displayName = "Participant")
    @Getter
    @Setter
    private Subject subject;

    @Field(required = true)
    @JsonSerialize(using = CustomDateSerializer.class)
    @Getter
    @Setter
    private LocalDate date;

    @Field
    @Getter
    @Setter
    private String purpose;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
