package dev.foliopdf;

/** Controls how child elements are distributed along the main axis of a flex or grid container. */
public enum JustifyContent {
    START(0),
    END(1),
    CENTER(2),
    SPACE_BETWEEN(3),
    SPACE_AROUND(4),
    SPACE_EVENLY(5);

    private final int value;

    JustifyContent(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this justify-content value.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
