package io.drogue.iot.vorto;

/**
 * Canonical error information.
 */
public class ErrorInformation {

    private final String error;
    private final String message;

    public ErrorInformation(final String error, final String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() {
        return this.error;
    }

    public String getMessage() {
        return this.message;
    }
}
