package dev.foliopdf;

/** Controls how child elements are aligned along the cross axis of a flex or grid container. */
public enum AlignItems {
    STRETCH(0),
    START(1),
    END(2),
    CENTER(3);

    private final int value;

    AlignItems(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this align-items value.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
