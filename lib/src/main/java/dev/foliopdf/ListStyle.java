package dev.foliopdf;

/** Marker style options for ordered and unordered list elements. */
public enum ListStyle {
    BULLET(0),
    DECIMAL(1),
    LOWER_ALPHA(2),
    UPPER_ALPHA(3),
    LOWER_ROMAN(4),
    UPPER_ROMAN(5);

    private final int value;

    ListStyle(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this list style.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
