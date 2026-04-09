package dev.foliopdf;

/**
 * Unit type for a {@link Grid} track (column or row) size definition.
 *
 * <ul>
 *   <li>{@link #PX} — fixed size in points
 *   <li>{@link #PERCENT} — percentage of the available space
 *   <li>{@link #FR} — fractional unit of the remaining free space
 *   <li>{@link #AUTO} — sized to fit content; the {@code value} is ignored
 * </ul>
 */
public enum GridTrackType {
    PX(0),
    PERCENT(1),
    FR(2),
    AUTO(3);

    private final int value;

    GridTrackType(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this track type.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
