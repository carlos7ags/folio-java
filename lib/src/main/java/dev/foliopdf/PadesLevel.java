package dev.foliopdf;

/**
 * PAdES (PDF Advanced Electronic Signatures) conformance levels.
 *
 * <ul>
 *   <li>{@link #B_B} — basic signature
 *   <li>{@link #B_T} — basic + timestamp
 *   <li>{@link #B_LT} — + revocation data (long-term validation)
 *   <li>{@link #B_LTA} — + document timestamp (long-term archival)
 * </ul>
 */
public enum PadesLevel {
    B_B(0),
    B_T(1),
    B_LT(2),
    B_LTA(3);

    private final int value;

    PadesLevel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
