package org.motechproject.umurinzi.exception;

public class UmurinziExportException extends RuntimeException {

    public UmurinziExportException(String message, Throwable cause) {
        super(message, cause);
    }

    public UmurinziExportException(String message) {
        super(message);
    }
}
