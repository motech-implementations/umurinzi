package org.motechproject.umurinzi.domain;

import java.util.ArrayList;
import java.util.List;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.enums.Language;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomDateTimeDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateTimeSerializer;
import org.motechproject.umurinzi.util.serializer.CustomVisitListDeserializer;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;

/**
 * Models data for registration of Subject
 */
@Entity(recordHistory = true, name = "Participant", maxFetchDepth = 3)
@NoArgsConstructor
public class Subject {
    public static final String SUBJECT_ID_FIELD_NAME = "subjectId";
    public static final String SUBJECT_ID_FIELD_DISPLAY_NAME = "Participant Id";

    @Unique
    @NonEditable
    @UIDisplayable(position = 0)
    @Field(required = true, displayName = SUBJECT_ID_FIELD_DISPLAY_NAME)
    @Getter
    @Setter
    private String subjectId;

    @UIDisplayable(position = 1)
    @Column(length = 20)
    @Pattern(regexp = "^[0-9\\s]*$")
    @Field
    @Getter
    private String phoneNumber;

    @UIDisplayable(position = 2)
    @Column(length = 20)
    @Field
    @Getter
    @Setter
    private Language language;

    @UIDisplayable(position = 3)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Field
    @Getter
    @Setter
    private LocalDate primerVaccinationDate;

    @UIDisplayable(position = 4)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Field
    @Getter
    @Setter
    private LocalDate boosterVaccinationDate;

    /**
     * Other fields
     */
    @UIDisplayable(position = 6)
    @JsonDeserialize(using = CustomVisitListDeserializer.class)
    @Field
    @Persistent(mappedBy = "subject")
    @Cascade(delete = true)
    @Getter
    @Setter
    private List<Visit> visits = new ArrayList<>();

    @NonEditable(display = false)
    @Field
    @Getter
    @Setter
    private String name;

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

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime creationDate;

    @JsonSerialize(using = CustomDateTimeSerializer.class)
    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @Field
    @Getter
    @Setter
    private DateTime modificationDate;

    public Subject(Subject subject) {
        subjectId = subject.getSubjectId();
        phoneNumber = subject.getPhoneNumber();
        language = subject.getLanguage();
        primerVaccinationDate = subject.getPrimerVaccinationDate();
        boosterVaccinationDate = subject.getBoosterVaccinationDate();
        name = subject.getName();
    }

    public void setPhoneNumber(String phoneNumber) {
        if (StringUtils.isEmpty(phoneNumber)) {
            this.phoneNumber = null;
        } else {
            this.phoneNumber = phoneNumber;
        }
    }

    @Ignore
    public void addVisit(Visit visit) {
        visits.add(visit);
    }

    @Ignore
    public void removeVisit(Visit visit) {
        visits.remove(visit);
    }

    @Override
    public String toString() {
        return subjectId;
    }
}
