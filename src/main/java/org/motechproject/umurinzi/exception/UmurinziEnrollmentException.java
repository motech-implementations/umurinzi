package org.motechproject.umurinzi.exception;

public class UmurinziEnrollmentException extends UmurinziException {

    public UmurinziEnrollmentException(String message, Throwable cause, String... params) {
        super(message, cause, params);
    }

    public UmurinziEnrollmentException(String message, String... params) {
        super(message, params);
    }
}
