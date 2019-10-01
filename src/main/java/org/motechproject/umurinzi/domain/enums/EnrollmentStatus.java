package org.motechproject.umurinzi.domain.enums;

import lombok.Getter;

public enum EnrollmentStatus {
    INITIAL("Initial"),
    ENROLLED("Enrolled"),
    UNENROLLED("Unenrolled"),
    COMPLETED("Completed");

    @Getter
    private String value;

    EnrollmentStatus(String value) {
        this.value = value;
    }
}
