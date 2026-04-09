package dev.foliopdf;

/** Controls whether children of a {@link Flex} container wrap onto multiple lines. */
public enum FlexWrap {
    NOWRAP(0),
    WRAP(1);

    private final int value;

    FlexWrap(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this wrap setting.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
