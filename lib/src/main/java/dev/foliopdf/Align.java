package dev.foliopdf;

/** Horizontal text alignment options for paragraphs, headings, links, and other elements. */
public enum Align {
    LEFT(0),
    CENTER(1),
    RIGHT(2),
    JUSTIFY(3);

    private final int value;

    Align(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this alignment value.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
