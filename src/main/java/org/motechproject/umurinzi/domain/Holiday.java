package org.motechproject.umurinzi.domain;

import javax.jdo.annotations.Unique;
import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;

@Entity
public class Holiday {

    public static final String HOLIDAY_DATE_FIELD_NAME = "holidayDate";
    public static final String HOLIDAY_DATE_FIELD_DISPLAY_NAME = "Holiday Date";

    @UIDisplayable(position = 0)
    @Field
    @Getter
    @Setter
    private String holidayName;

    @Unique
    @UIDisplayable(position = 1)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Field(required = true)
    @Getter
    @Setter
    private LocalDate holidayDate;

    /**
     * Motech internal fields
     */
    @Field
    @Getter
    @Setter
    private Long id;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
