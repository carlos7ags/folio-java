package dev.foliopdf;

/**
 * QR code error correction capacity levels.
 *
 * <p>Higher levels allow more of the symbol to be damaged or obscured while still being
 * decodable, at the cost of a denser (larger) symbol.
 *
 * <ul>
 *   <li>{@link #L} — ~7% data recovery
 *   <li>{@link #M} — ~15% data recovery
 *   <li>{@link #Q} — ~25% data recovery
 *   <li>{@link #H} — ~30% data recovery
 * </ul>
 */
public enum ECCLevel {
    L(0),
    M(1),
    Q(2),
    H(3);

    private final int value;

    ECCLevel(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this error correction level.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
