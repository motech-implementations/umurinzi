package org.motechproject.umurinzi.exception;

public class UmurinziLookupException extends RuntimeException {

    public UmurinziLookupException(String message, Throwable cause) {
        super(message, cause);
    }

    public UmurinziLookupException(String message) {
        super(message);
    }
}
