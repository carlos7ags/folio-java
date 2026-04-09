package dev.foliopdf;

/** Main-axis direction for a {@link Flex} container. */
public enum FlexDirection {
    ROW(0),
    COLUMN(1);

    private final int value;

    FlexDirection(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this direction.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
