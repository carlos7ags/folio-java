package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A clickable hyperlink element that can be added to a document.
 *
 * <p>Supports external URIs, file-embedded links, and internal named-destination links.
 *
 * <pre>{@code
 * var link = Link.of("Visit our website", "https://example.com")
 *     .color(new Color(0, 0, 255))
 *     .underline();
 * doc.add(link);
 * }</pre>
 */
public final class Link implements Element {

    private final HandleRef handle;

    private Link(long handle) {
        this.handle = new HandleRef(handle, FolioNative::linkFree);
    }

    /**
     * Creates a link with the given display text and URI using Helvetica at 12pt.
     *
     * @param text the visible link text
     * @param uri  the target URI
     * @return a new {@code Link} instance
     */
    public static Link of(String text, String uri) {
        return of(text, uri, Font.helvetica(), 12);
    }

    /**
     * Creates a link with the given display text, URI, font, and font size.
     *
     * @param text     the visible link text
     * @param uri      the target URI
     * @param font     the font to use for the link text
     * @param fontSize the font size in points
     * @return a new {@code Link} instance
     * @throws FolioException if the native link cannot be created
     */
    public static Link of(String text, String uri, Font font, double fontSize) {
        long h = FolioNative.linkNew(text, uri, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create link: " + FolioNative.lastError());
        return new Link(h);
    }

    /**
     * Creates a link using an embedded (subset) font.
     *
     * @param text     the visible link text
     * @param uri      the target URI
     * @param font     the embedded font
     * @param fontSize the font size in points
     * @return a new {@code Link} instance
     * @throws FolioException if the native link cannot be created
     */
    public static Link ofEmbedded(String text, String uri, Font font, double fontSize) {
        long h = FolioNative.linkNewEmbedded(text, uri, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create embedded link: " + FolioNative.lastError());
        return new Link(h);
    }

    /**
     * Creates an internal link that navigates to a named destination within the same document.
     *
     * @param text      the visible link text
     * @param destName  the name of the destination anchor in the document
     * @param font      the font to use for the link text
     * @param fontSize  the font size in points
     * @return a new {@code Link} instance
     * @throws FolioException if the native link cannot be created
     */
    public static Link internal(String text, String destName, Font font, double fontSize) {
        long h = FolioNative.linkNewInternal(text, destName, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create internal link: " + FolioNative.lastError());
        return new Link(h);
    }

    /**
     * Sets the text color of the link.
     *
     * @param color the RGB color to apply
     * @return this instance for chaining
     */
    public Link color(Color color) {
        FolioNative.linkSetColor(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Enables underlining for the link text.
     *
     * @return this instance for chaining
     */
    public Link underline() {
        FolioNative.linkSetUnderline(handle.get());
        return this;
    }

    /**
     * Sets the horizontal alignment of the link within its container.
     *
     * @param align the desired alignment
     * @return this instance for chaining
     */
    public Link align(Align align) {
        FolioNative.linkSetAlign(handle.get(), align.value());
        return this;
    }

    /**
     * Returns the native handle for this link.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
