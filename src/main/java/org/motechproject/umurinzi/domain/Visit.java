package org.motechproject.umurinzi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomSubjectSerializer;
import org.motechproject.umurinzi.util.serializer.CustomVisitTypeDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomVisitTypeSerializer;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.EnumDisplayName;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;

@Entity(recordHistory = true, maxFetchDepth = 2)
@NoArgsConstructor
public class Visit {

    public static final String VISIT_TYPE_PROPERTY_NAME = "type";
    public static final String VISIT_PLANNED_DATE_PROPERTY_NAME = "dateProjected";
    public static final String VISIT_DATE_PROPERTY_NAME = "date";

    public static final String SUBJECT_PHONE_NUMBER_PROPERTY_NAME = "subject.phoneNumber";

    public static final String VISIT_TYPE_DISPLAY_NAME = "Visit Type";

    @Field
    @Getter
    @Setter
    private Long id;

    @JsonSerialize(using = CustomSubjectSerializer.class)
    @NonEditable
    @Field(required = true, displayName = "Participant")
    @Getter
    @Setter
    private Subject subject;

    @JsonSerialize(using = CustomVisitTypeSerializer.class)
    @JsonDeserialize(using = CustomVisitTypeDeserializer.class)
    @NonEditable
    @Field(displayName = VISIT_TYPE_DISPLAY_NAME, required = true)
    @EnumDisplayName(enumField = "displayValue")
    @Getter
    @Setter
    private VisitType type;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field(displayName = "Actual Visit Date")
    @Getter
    @Setter
    private LocalDate date;

    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field(displayName = "Planned Visit Date")
    @Getter
    @Setter
    private LocalDate dateProjected;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;

    public Visit(Subject subject, VisitType type) {
        this.subject = subject;
        this.type = type;
    }

    @Override
    public String toString() {
        return type.getDisplayValue() +
                (getDateProjected() != null ? " / Planned Date: " + getDateProjected().toString() : "") +
                (getDate() != null ? " / Actual Date: " + getDate().toString() : "");
    }
}
