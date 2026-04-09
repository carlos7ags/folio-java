package dev.foliopdf;

/** Heading level constants corresponding to HTML heading tags H1 through H6. */
public enum HeadingLevel {
    H1(1), H2(2), H3(3), H4(4), H5(5), H6(6);

    private final int value;

    HeadingLevel(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric heading level (1–6).
     *
     * @return the heading level integer
     */
    public int value() {
        return value;
    }
}
