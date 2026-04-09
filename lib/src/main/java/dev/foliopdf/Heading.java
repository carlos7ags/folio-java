package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents a section heading at a specified level (H1–H6).
 *
 * <pre>{@code
 * doc.add(Heading.of("Introduction", HeadingLevel.H1));
 * doc.add(Heading.of("Background", HeadingLevel.H2, Font.timesBold(), 14));
 * }</pre>
 */
public final class Heading implements Element {

    private final HandleRef handle;

    private Heading(long handle) {
        this.handle = new HandleRef(handle, FolioNative::headingFree);
    }

    /**
     * Creates a heading with the given text and level using the default font.
     *
     * @param text  the heading text
     * @param level the {@link HeadingLevel} (H1–H6)
     * @return a new {@link Heading}
     * @throws FolioException if the native call fails
     */
    public static Heading of(String text, HeadingLevel level) {
        long h = FolioNative.headingNew(text, level.value());
        if (h == 0) throw new FolioException("Failed to create heading: " + FolioNative.lastError());
        return new Heading(h);
    }

    /**
     * Creates a heading with a custom font and font size.
     *
     * @param text     the heading text
     * @param level    the {@link HeadingLevel} (H1–H6)
     * @param font     the font to use
     * @param fontSize the font size in points
     * @return a new {@link Heading}
     * @throws FolioException if the native call fails
     */
    public static Heading of(String text, HeadingLevel level, Font font, double fontSize) {
        long h = FolioNative.headingNewWithFont(text, level.value(), font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create heading with font: " + FolioNative.lastError());
        return new Heading(h);
    }

    /**
     * Creates a heading that embeds the font subset in the PDF output.
     *
     * @param text  the heading text
     * @param level the {@link HeadingLevel} (H1–H6)
     * @param font  the font to embed
     * @return a new {@link Heading} with an embedded font
     * @throws FolioException if the native call fails
     */
    public static Heading ofEmbedded(String text, HeadingLevel level, Font font) {
        long h = FolioNative.headingNewEmbedded(text, level.value(), font.handle());
        if (h == 0) throw new FolioException("Failed to create embedded heading: " + FolioNative.lastError());
        return new Heading(h);
    }

    /**
     * Sets the text alignment for this heading.
     *
     * @param align the desired {@link Align} value
     * @return this heading, for chaining
     */
    /**
     * Replaces the heading text with styled runs from a {@link RunList}.
     *
     * @param runs the run list containing styled text segments
     * @return this heading, for chaining
     */
    public Heading setRuns(RunList runs) {
        FolioNative.headingSetRuns(handle.get(), runs.handle());
        return this;
    }

    public Heading align(Align align) {
        FolioNative.headingSetAlign(handle.get(), align.value());
        return this;
    }

    /**
     * Returns the native handle for this heading.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }
}
