package org.motechproject.umurinzi.exception;

public class UmurinziReportException extends UmurinziException {

    public UmurinziReportException(String message, Throwable cause, String... params) {
        super(message, cause,  params);
    }

    public UmurinziReportException(String message, String... params) {
        super(message, params);
    }
}
