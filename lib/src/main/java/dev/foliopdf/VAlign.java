package dev.foliopdf;

/** Vertical alignment options for table cells and other block containers. */
public enum VAlign {
    TOP(0),
    MIDDLE(1),
    BOTTOM(2);

    private final int value;

    VAlign(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this vertical alignment value.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
