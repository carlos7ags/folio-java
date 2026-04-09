package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Accumulates styled text runs for use with headings and list items.
 * Supports mixed fonts, colors, links, and decorations within a single element.
 *
 * <pre>{@code
 * var runs = RunList.of()
 *     .add("Read the ", Font.helvetica(), 11, Color.BLACK)
 *     .addLink("docs", Font.helveticaBold(), 11, Color.BLUE, "https://example.com", true)
 *     .add(" for details.", Font.helvetica(), 11, Color.BLACK);
 * heading.setRuns(runs);
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class RunList implements AutoCloseable {

    private final HandleRef handle;

    private RunList(long handle) {
        this.handle = new HandleRef(handle, FolioNative::runListFree);
    }

    /**
     * Creates a new empty run list.
     *
     * @return a new {@code RunList} instance
     * @throws FolioException if the native run list cannot be created
     */
    public static RunList of() {
        long h = FolioNative.runListNew();
        if (h == 0) throw new FolioException("Failed to create run list: " + FolioNative.lastError());
        return new RunList(h);
    }

    /**
     * Appends a styled text run using a standard (non-embedded) font.
     *
     * @param text     the text content for this run
     * @param font     the font to render with
     * @param fontSize the font size in points
     * @param color    the text color
     * @return this run list, for chaining
     */
    public RunList add(String text, Font font, double fontSize, Color color) {
        FolioNative.runListAdd(handle.get(), text, font.handle(), fontSize, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Appends a styled text run using an embedded font subset.
     *
     * @param text     the text content for this run
     * @param font     the embedded font to render with
     * @param fontSize the font size in points
     * @param color    the text color
     * @return this run list, for chaining
     */
    public RunList addEmbedded(String text, Font font, double fontSize, Color color) {
        FolioNative.runListAddEmbedded(handle.get(), text, font.handle(), fontSize, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Appends a clickable link run.
     *
     * @param text      the visible link text
     * @param font      the font to render with
     * @param fontSize  the font size in points
     * @param color     the text color
     * @param uri       the target URI
     * @param underline whether to draw an underline decoration
     * @return this run list, for chaining
     */
    public RunList addLink(String text, Font font, double fontSize, Color color, String uri, boolean underline) {
        FolioNative.runListAddLink(handle.get(), text, font.handle(), fontSize, color.r(), color.g(), color.b(), uri, underline);
        return this;
    }

    /**
     * Applies underline decoration to the last added run.
     *
     * @return this run list, for chaining
     */
    public RunList lastUnderline() {
        FolioNative.runListLastSetUnderline(handle.get());
        return this;
    }

    /**
     * Applies strikethrough decoration to the last added run.
     *
     * @return this run list, for chaining
     */
    public RunList lastStrikethrough() {
        FolioNative.runListLastSetStrikethrough(handle.get());
        return this;
    }

    /**
     * Sets letter spacing on the last added run.
     *
     * @param spacing the letter spacing in points
     * @return this run list, for chaining
     */
    public RunList lastLetterSpacing(double spacing) {
        FolioNative.runListLastSetLetterSpacing(handle.get(), spacing);
        return this;
    }

    /**
     * Applies a highlight background color to the last added run.
     *
     * @param r red channel in {@code [0.0, 1.0]}
     * @param g green channel in {@code [0.0, 1.0]}
     * @param b blue channel in {@code [0.0, 1.0]}
     * @return this run list, for chaining
     */
    public RunList lastSetBackgroundColor(double r, double g, double b) {
        FolioNative.runListLastSetBackgroundColor(handle.get(), r, g, b);
        return this;
    }

    /**
     * Applies a highlight background color to the last added run.
     *
     * @param color the highlight color
     * @return this run list, for chaining
     */
    public RunList lastSetBackgroundColor(Color color) {
        return lastSetBackgroundColor(color.r(), color.g(), color.b());
    }

    /**
     * Returns the native handle for passing to heading/list methods.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }

    @Override
    public void close() {
        handle.close();
    }
}
