package org.motechproject.umurinzi.exception;

public class UmurinziException extends RuntimeException {

    public UmurinziException(String message, Throwable cause, String... params) {
        this(String.format(message, params), cause);
    }

    public UmurinziException(String message, String... params) {
        this(String.format(message, params));
    }

    public UmurinziException(String message, Throwable cause) {
        super(message, cause);
    }

    public UmurinziException(String message) {
        super(message);
    }
}
