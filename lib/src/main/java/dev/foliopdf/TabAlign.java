package dev.foliopdf;

/** Alignment of text within a tab stop of a {@link TabbedLine}. */
public enum TabAlign {
    LEFT(0),
    RIGHT(1),
    CENTER(2);

    private final int value;

    TabAlign(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this tab alignment.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
