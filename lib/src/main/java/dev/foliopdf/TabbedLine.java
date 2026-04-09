package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A line of text divided into tab-stop segments, useful for layouts like tables of contents
 * or form fields where text must align at specific horizontal positions.
 *
 * <pre>{@code
 * var line = TabbedLine.of(
 *         Font.helvetica(), 12,
 *         new double[]{0, 200, 400},
 *         new TabAlign[]{TabAlign.LEFT, TabAlign.CENTER, TabAlign.RIGHT},
 *         new int[]{0, 0, 1})   // leader on last segment
 *     .segments("Introduction", "Chapter 1", "5");
 * doc.add(line);
 * }</pre>
 */
public final class TabbedLine implements Element {

    private final HandleRef handle;

    private TabbedLine(long handle) {
        this.handle = new HandleRef(handle, FolioNative::tabbedLineFree);
    }

    /**
     * Creates a tabbed line using a standard (non-embedded) font.
     *
     * @param font      the font for all segments
     * @param fontSize  the font size in points
     * @param positions the tab-stop positions in points from the left margin
     * @param aligns    the alignment of text at each tab stop
     * @param leaders   leader style codes for each segment (0 = none)
     * @return a new {@code TabbedLine} instance
     * @throws FolioException if the native tabbed line cannot be created
     */
    public static TabbedLine of(Font font, double fontSize, double[] positions, TabAlign[] aligns, int[] leaders) {
        int[] alignValues = new int[aligns.length];
        for (int i = 0; i < aligns.length; i++) alignValues[i] = aligns[i].value();
        long h = FolioNative.tabbedLineNew(font.handle(), fontSize, positions, alignValues, leaders);
        if (h == 0) throw new FolioException("Failed to create tabbed line: " + FolioNative.lastError());
        return new TabbedLine(h);
    }

    /**
     * Creates a tabbed line using an embedded (subset) font.
     *
     * @param font      the embedded font for all segments
     * @param fontSize  the font size in points
     * @param positions the tab-stop positions in points from the left margin
     * @param aligns    the alignment of text at each tab stop
     * @param leaders   leader style codes for each segment (0 = none)
     * @return a new {@code TabbedLine} instance
     * @throws FolioException if the native tabbed line cannot be created
     */
    public static TabbedLine ofEmbedded(Font font, double fontSize, double[] positions, TabAlign[] aligns, int[] leaders) {
        int[] alignValues = new int[aligns.length];
        for (int i = 0; i < aligns.length; i++) alignValues[i] = aligns[i].value();
        long h = FolioNative.tabbedLineNewEmbedded(font.handle(), fontSize, positions, alignValues, leaders);
        if (h == 0) throw new FolioException("Failed to create embedded tabbed line: " + FolioNative.lastError());
        return new TabbedLine(h);
    }

    /**
     * Sets the text content of each tab segment. The number of strings should match the number
     * of tab stops defined at construction time.
     *
     * @param segments the text for each tab stop, in order
     * @return this instance for chaining
     */
    public TabbedLine segments(String... segments) {
        FolioNative.tabbedLineSetSegments(handle.get(), segments);
        return this;
    }

    /**
     * Sets the text color for this tabbed line.
     *
     * @param color the RGB color
     * @return this instance for chaining
     */
    public TabbedLine color(Color color) {
        FolioNative.tabbedLineSetColor(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the line-height multiplier for this tabbed line.
     *
     * @param leading the leading multiplier (e.g. {@code 1.5} for 150% line height)
     * @return this instance for chaining
     */
    public TabbedLine leading(double leading) {
        FolioNative.tabbedLineSetLeading(handle.get(), leading);
        return this;
    }

    /**
     * Returns the native handle for this tabbed line.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
