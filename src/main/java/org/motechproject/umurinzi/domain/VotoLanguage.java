package org.motechproject.umurinzi.domain;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.umurinzi.domain.enums.Language;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.NonEditable;

@Entity
public class VotoLanguage {

    @Field
    @Getter
    @Setter
    private String votoId;

    @Field
    @Getter
    @Setter
    private Language language;

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String owner;
}
