package dev.foliopdf;

/** The side of the page to which a {@link FloatElement} is anchored. */
public enum FloatSide {
    LEFT(0),
    RIGHT(1);

    private final int value;

    FloatSide(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this float side.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
