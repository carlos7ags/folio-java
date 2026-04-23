package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents a styled paragraph of text. Create instances using {@link #of(String)} or
 * {@link #of(String, Font, double)}, then chain fluent setters before adding to a document.
 *
 * <pre>{@code
 * doc.add(Paragraph.of("Hello, world!")
 *         .align(Align.CENTER)
 *         .leading(1.4)
 *         .spaceBefore(6));
 * }</pre>
 */
public final class Paragraph implements Element {

    private final HandleRef handle;

    private Paragraph(long handle) {
        this.handle = new HandleRef(handle, FolioNative::paragraphFree);
    }

    /**
     * Creates a paragraph with the given text using the default Helvetica 12pt font.
     *
     * @param text the paragraph text
     * @return a new {@link Paragraph}
     * @throws FolioException if the native call fails
     */
    public static Paragraph of(String text) {
        return of(text, Font.helvetica(), 12);
    }

    /**
     * Creates a paragraph with the given text, font, and font size.
     *
     * @param text     the paragraph text
     * @param font     the font to use
     * @param fontSize the font size in points
     * @return a new {@link Paragraph}
     * @throws FolioException if the native call fails
     */
    public static Paragraph of(String text, Font font, double fontSize) {
        long h = FolioNative.paragraphNew(text, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create paragraph: " + FolioNative.lastError());
        return new Paragraph(h);
    }

    /**
     * Creates a paragraph that embeds the font subset in the PDF output.
     *
     * @param text     the paragraph text
     * @param font     the font to embed
     * @param fontSize the font size in points
     * @return a new {@link Paragraph} with an embedded font
     * @throws FolioException if the native call fails
     */
    /**
     * Creates a paragraph with the given text and font size using the default Helvetica font.
     *
     * @param text     the paragraph text
     * @param fontSize the font size in points
     * @return a new {@link Paragraph}
     */
    public static Paragraph of(String text, double fontSize) {
        return of(text, Font.helvetica(), fontSize);
    }

    public static Paragraph ofEmbedded(String text, Font font, double fontSize) {
        long h = FolioNative.paragraphNewEmbedded(text, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create embedded paragraph: " + FolioNative.lastError());
        return new Paragraph(h);
    }

    /**
     * Sets the text alignment for this paragraph.
     *
     * @param align the desired {@link Align} value
     * @return this paragraph, for chaining
     */
    public Paragraph align(Align align) {
        FolioNative.paragraphSetAlign(handle.get(), align.value());
        return this;
    }

    /**
     * Sets the line-height multiplier for this paragraph.
     *
     * @param leading line height as a multiple of the font size (e.g., {@code 1.5})
     * @return this paragraph, for chaining
     */
    public Paragraph leading(double leading) {
        FolioNative.paragraphSetLeading(handle.get(), leading);
        return this;
    }

    /**
     * Sets the amount of space to add before this paragraph.
     *
     * @param pts vertical space in points
     * @return this paragraph, for chaining
     */
    public Paragraph spaceBefore(double pts) {
        FolioNative.paragraphSetSpaceBefore(handle.get(), pts);
        return this;
    }

    /**
     * Sets the amount of space to add after this paragraph.
     *
     * @param pts vertical space in points
     * @return this paragraph, for chaining
     */
    public Paragraph spaceAfter(double pts) {
        FolioNative.paragraphSetSpaceAfter(handle.get(), pts);
        return this;
    }

    /**
     * Sets the background color behind the paragraph text.
     *
     * @param color the background {@link Color}
     * @return this paragraph, for chaining
     */
    public Paragraph background(Color color) {
        FolioNative.paragraphSetBackground(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the first-line indent for this paragraph.
     *
     * @param pts indent in points
     * @return this paragraph, for chaining
     */
    public Paragraph firstIndent(double pts) {
        FolioNative.paragraphSetFirstIndent(handle.get(), pts);
        return this;
    }

    /**
     * Sets the minimum number of lines to keep at the bottom of a page (orphan control).
     *
     * @param n minimum orphan lines
     * @return this paragraph, for chaining
     */
    public Paragraph orphans(int n) {
        FolioNative.paragraphSetOrphans(handle.get(), n);
        return this;
    }

    /**
     * Sets the minimum number of lines to keep at the top of a page (widow control).
     *
     * @param n minimum widow lines
     * @return this paragraph, for chaining
     */
    public Paragraph widows(int n) {
        FolioNative.paragraphSetWidows(handle.get(), n);
        return this;
    }

    /**
     * Enables or disables ellipsis truncation when text overflows.
     *
     * @param enabled {@code true} to append an ellipsis on overflow
     * @return this paragraph, for chaining
     */
    public Paragraph ellipsis(boolean enabled) {
        FolioNative.paragraphSetEllipsis(handle.get(), enabled);
        return this;
    }

    /**
     * Sets the word-break mode for this paragraph.
     *
     * @param mode word-break mode string (e.g., {@code "break-all"})
     * @return this paragraph, for chaining
     */
    public Paragraph wordBreak(String mode) {
        FolioNative.paragraphSetWordBreak(handle.get(), mode);
        return this;
    }

    /**
     * Sets the hyphenation mode for this paragraph.
     *
     * @param mode hyphenation mode string (e.g., {@code "auto"})
     * @return this paragraph, for chaining
     */
    public Paragraph hyphens(String mode) {
        FolioNative.paragraphSetHyphens(handle.get(), mode);
        return this;
    }

    /**
     * Appends a styled text run to this paragraph.
     *
     * @param text     the run text
     * @param font     the font for this run
     * @param fontSize the font size in points
     * @param color    the text color
     * @return this paragraph, for chaining
     */
    public Paragraph addRun(String text, Font font, double fontSize, Color color) {
        FolioNative.paragraphAddRun(handle.get(), text, font.handle(), fontSize, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Controls the alignment of the last line in a justified paragraph.
     *
     * @param align alignment to apply to the final line
     * @return this paragraph, for chaining
     */
    public Paragraph setTextAlignLast(Align align) {
        FolioNative.paragraphSetTextAlignLast(handle.get(), align.value());
        return this;
    }

    /**
     * Sets the writing direction (LTR, RTL, or AUTO) for this paragraph.
     *
     * <p>{@link Direction#AUTO} runs the Unicode Bidi algorithm over the
     * paragraph contents to infer direction from the dominant script.
     * Direction influences the {@code /Lang} entry and structure attributes
     * in tagged-PDF output (ISO 32000-2 §14.8.2).
     *
     * @param direction the desired {@link Direction}
     * @return this paragraph, for chaining
     * @since 0.7.0
     */
    public Paragraph setDirection(Direction direction) {
        FolioNative.paragraphSetDirection(handle.get(), direction.value());
        return this;
    }

    /**
     * Returns the native handle for this paragraph.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }
}
