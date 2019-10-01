package org.motechproject.umurinzi.exception;

public class UmurinziInitiateCallException extends UmurinziException {

    public UmurinziInitiateCallException(String message, Throwable cause, String... params) {
        super(message, cause, params);
    }

    public UmurinziInitiateCallException(String message, String... params) {
        super(message, params);
    }
}
