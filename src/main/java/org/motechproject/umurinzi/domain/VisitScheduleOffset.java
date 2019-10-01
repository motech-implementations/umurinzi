package org.motechproject.umurinzi.domain;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.umurinzi.domain.enums.VisitType;

import javax.jdo.annotations.Unique;

@Entity
public class VisitScheduleOffset {

    @Unique
    @UIDisplayable(position = 0)
    @Field(required = true)
    @Getter
    @Setter
    private VisitType visitType;

    @UIDisplayable(position = 1)
    @Field(required = true)
    @Getter
    @Setter
    private Integer timeOffset;

    @UIDisplayable(position = 2)
    @Field
    @Getter
    @Setter
    private Integer earliestDateOffset;

    @UIDisplayable(position = 3)
    @Field
    @Getter
    @Setter
    private Integer latestDateOffset;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
