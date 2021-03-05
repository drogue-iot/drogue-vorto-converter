package io.drogue.iot.vorto;

public final class Attributes {
    private Attributes() {
    }

    public static boolean isNonEmptyString(final Object value) {
        if (!(value instanceof String)) {
            return false;
        }

        return !((String) value).isBlank();
    }
}
