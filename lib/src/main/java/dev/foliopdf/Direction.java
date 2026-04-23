package dev.foliopdf;

/**
 * Writing direction for paragraphs, lists, and tables.
 *
 * <p>The numeric values correspond to the {@code FOLIO_DIR_*} macros declared in
 * the C ABI header ({@code folio.h}):
 * <ul>
 *   <li>{@link #AUTO} = {@code FOLIO_DIR_AUTO} (0) — direction is inferred from
 *       the script of the content (Bidi heuristic).</li>
 *   <li>{@link #LTR} = {@code FOLIO_DIR_LTR} (1) — force left-to-right.</li>
 *   <li>{@link #RTL} = {@code FOLIO_DIR_RTL} (2) — force right-to-left, which
 *       also right-aligns by default and reverses table column order.</li>
 * </ul>
 *
 * <p>Direction interacts with the PDF {@code /Lang} entry and tagged-PDF
 * structure attributes; see ISO 32000-2 §14.8.2 (Structure Attributes).
 *
 * @since 0.7.0
 */
public enum Direction {

    /** Automatically detect direction from the content's script. */
    AUTO(0),

    /** Force left-to-right writing direction. */
    LTR(1),

    /** Force right-to-left writing direction. */
    RTL(2);

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this direction value, matching the
     * {@code FOLIO_DIR_*} macros in the C ABI.
     *
     * @return the native integer constant (0 = AUTO, 1 = LTR, 2 = RTL)
     * @since 0.7.0
     */
    public int value() {
        return value;
    }
}
