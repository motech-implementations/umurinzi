package org.motechproject.umurinzi.utils;

import org.motechproject.umurinzi.domain.Subject;

public final class SubjectUtil {

    private SubjectUtil() {
    }

    public static Subject createSubject(String subjectId, String name, String phoneNumber) {
        Subject subject = new Subject();
        subject.setSubjectId(subjectId);
        subject.setName(name);
        subject.setPhoneNumber(phoneNumber);
        return subject;
    }
}
