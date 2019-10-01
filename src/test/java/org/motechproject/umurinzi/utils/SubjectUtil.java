package org.motechproject.umurinzi.utils;

import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.enums.Language;

public final class SubjectUtil {

    private SubjectUtil() {
    }

    public static Subject createSubject(String subjectId, String name, String phoneNumber, Language language) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setName(name);
        subject.setPhoneNumber(phoneNumber);
        subject.setLanguage(language);
        return subject;
    }
}
