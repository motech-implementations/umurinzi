package org.motechproject.umurinzi.domain;

import java.util.ArrayList;
import java.util.List;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.motechproject.mds.annotations.Cascade;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.Ignore;
import org.motechproject.mds.annotations.NonEditable;
import org.motechproject.mds.annotations.UIDisplayable;
import org.motechproject.umurinzi.util.serializer.CustomDateDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateSerializer;
import org.motechproject.umurinzi.util.serializer.CustomDateTimeDeserializer;
import org.motechproject.umurinzi.util.serializer.CustomDateTimeSerializer;
import org.motechproject.umurinzi.util.serializer.CustomVisitListDeserializer;

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
    @Field
    @Getter
    private String phoneNumber;

    @UIDisplayable(position = 2)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate primeVaccinationDate;

    @UIDisplayable(position = 3)
    @JsonSerialize(using = CustomDateSerializer.class)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @NonEditable
    @Field
    @Getter
    @Setter
    private LocalDate boostVaccinationDate;

    @UIDisplayable(position = 4)
    @Column
    @Field
    @Getter
    @Setter
    private String helpLine;

    /**
     * Other fields
     */
    @JsonDeserialize(using = CustomVisitListDeserializer.class)
    @NonEditable(display = false)
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
        primeVaccinationDate = subject.getPrimeVaccinationDate();
        boostVaccinationDate = subject.getBoostVaccinationDate();
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
