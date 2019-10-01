package org.motechproject.umurinzi.domain;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;

@Entity
public class VotoMessage {

    @Field
    @Getter
    @Setter
    private String votoIvrId;

    @Field
    @Getter
    @Setter
    private String votoSmsId;

    @Field
    @Getter
    @Setter
    private String messageKey;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
