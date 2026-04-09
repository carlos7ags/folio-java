package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * Represents a single page within a {@link Document}, providing low-level absolute
 * positioning for text, images, links, and annotations. Obtain a {@code Page} via
 * {@link Document#addPage()}.
 *
 * <pre>{@code
 * Page page = doc.addPage();
 * page.addText("Hello", Font.helvetica(), 12, 72, 700)
 *     .addImage(img, 72, 600, 100, 75);
 * }</pre>
 */
public final class Page {

    private final long handle;

    public Page(long handle) {
        this.handle = handle;
    }

    /**
     * Places text at an absolute position on this page using a standard font.
     *
     * @param text the text to draw
     * @param font the font to use
     * @param size the font size in points
     * @param x    x coordinate in points from the left edge
     * @param y    y coordinate in points from the bottom edge
     * @return this page, for chaining
     */
    public Page addText(String text, Font font, double size, double x, double y) {
        FolioNative.pageAddText(handle, text, font.handle(), size, x, y);
        return this;
    }

    /**
     * Places text at an absolute position using a font whose subset is embedded.
     *
     * @param text the text to draw
     * @param font the font to embed
     * @param size the font size in points
     * @param x    x coordinate in points from the left edge
     * @param y    y coordinate in points from the bottom edge
     * @return this page, for chaining
     */
    public Page addTextEmbedded(String text, Font font, double size, double x, double y) {
        FolioNative.pageAddTextEmbedded(handle, text, font.handle(), size, x, y);
        return this;
    }

    /**
     * Draws an image at an absolute position and size on this page.
     *
     * @param image  the image to draw
     * @param x      x coordinate in points from the left edge
     * @param y      y coordinate in points from the bottom edge
     * @param width  display width in points
     * @param height display height in points
     * @return this page, for chaining
     */
    public Page addImage(Image image, double x, double y, double width, double height) {
        FolioNative.pageAddImage(handle, image.imageHandle(), x, y, width, height);
        return this;
    }

    /**
     * Adds a URI hyperlink annotation over the specified rectangle.
     *
     * @param x1  left coordinate in points
     * @param y1  bottom coordinate in points
     * @param x2  right coordinate in points
     * @param y2  top coordinate in points
     * @param uri the target URI
     * @return this page, for chaining
     */
    public Page addLink(double x1, double y1, double x2, double y2, String uri) {
        FolioNative.pageAddLink(handle, x1, y1, x2, y2, uri);
        return this;
    }

    /**
     * Sets a uniform opacity for all content drawn on this page.
     *
     * @param alpha opacity in the range {@code [0.0, 1.0]}
     * @return this page, for chaining
     */
    public Page opacity(double alpha) {
        FolioNative.pageSetOpacity(handle, alpha);
        return this;
    }

    /**
     * Rotates this page by the given number of degrees (must be a multiple of 90).
     *
     * @param degrees rotation in degrees (e.g., 90, 180, 270)
     * @return this page, for chaining
     */
    public Page rotate(int degrees) {
        FolioNative.pageSetRotate(handle, degrees);
        return this;
    }

    /**
     * Adds an internal link annotation that navigates to a named destination.
     *
     * @param x1       left coordinate in points
     * @param y1       bottom coordinate in points
     * @param x2       right coordinate in points
     * @param y2       top coordinate in points
     * @param destName the target named destination
     * @return this page, for chaining
     */
    public Page addInternalLink(double x1, double y1, double x2, double y2, String destName) {
        FolioNative.pageAddInternalLink(handle, x1, y1, x2, y2, destName);
        return this;
    }

    /**
     * Adds a text (sticky-note) annotation on this page.
     *
     * @param x1   left coordinate in points
     * @param y1   bottom coordinate in points
     * @param x2   right coordinate in points
     * @param y2   top coordinate in points
     * @param text the annotation content text
     * @param icon the icon name (e.g., {@code "Note"}, {@code "Comment"})
     * @return this page, for chaining
     */
    public Page addTextAnnotation(double x1, double y1, double x2, double y2, String text, String icon) {
        FolioNative.pageAddTextAnnotation(handle, x1, y1, x2, y2, text, icon);
        return this;
    }

    /**
     * Sets the crop box for this page, defining the visible region.
     *
     * @param x1 left coordinate in points
     * @param y1 bottom coordinate in points
     * @param x2 right coordinate in points
     * @param y2 top coordinate in points
     * @return this page, for chaining
     */
    public Page cropBox(double x1, double y1, double x2, double y2) {
        FolioNative.pageSetCropBox(handle, x1, y1, x2, y2);
        return this;
    }

    /**
     * Sets the trim box for this page, defining the intended final size after trimming.
     *
     * @param x1 left coordinate in points
     * @param y1 bottom coordinate in points
     * @param x2 right coordinate in points
     * @param y2 top coordinate in points
     * @return this page, for chaining
     */
    public Page trimBox(double x1, double y1, double x2, double y2) {
        FolioNative.pageSetTrimBox(handle, x1, y1, x2, y2);
        return this;
    }

    /**
     * Sets the bleed box for this page, defining the region to which content may bleed.
     *
     * @param x1 left coordinate in points
     * @param y1 bottom coordinate in points
     * @param x2 right coordinate in points
     * @param y2 top coordinate in points
     * @return this page, for chaining
     */
    public Page bleedBox(double x1, double y1, double x2, double y2) {
        FolioNative.pageSetBleedBox(handle, x1, y1, x2, y2);
        return this;
    }

    /**
     * Sets the art box for this page, defining the extent of meaningful content.
     *
     * @param x1 left coordinate in points
     * @param y1 bottom coordinate in points
     * @param x2 right coordinate in points
     * @param y2 top coordinate in points
     * @return this page, for chaining
     */
    public Page artBox(double x1, double y1, double x2, double y2) {
        FolioNative.pageSetArtBox(handle, x1, y1, x2, y2);
        return this;
    }

    /**
     * Sets the media box dimensions (page size) for this page.
     *
     * @param width  page width in points
     * @param height page height in points
     * @return this page, for chaining
     */
    public Page size(double width, double height) {
        FolioNative.pageSetSize(handle, width, height);
        return this;
    }

    /**
     * Adds a page-navigation link that jumps to another page in the document.
     *
     * @param x1         left coordinate in points
     * @param y1         bottom coordinate in points
     * @param x2         right coordinate in points
     * @param y2         top coordinate in points
     * @param targetPage zero-based target page index
     * @return this page, for chaining
     */
    public Page addPageLink(double x1, double y1, double x2, double y2, int targetPage) {
        FolioNative.pageAddPageLink(handle, x1, y1, x2, y2, targetPage);
        return this;
    }

    /**
     * Sets independent fill and stroke opacity for content drawn on this page.
     *
     * @param fillAlpha   fill opacity in the range {@code [0.0, 1.0]}
     * @param strokeAlpha stroke opacity in the range {@code [0.0, 1.0]}
     * @return this page, for chaining
     */
    public Page opacityFillStroke(double fillAlpha, double strokeAlpha) {
        FolioNative.pageSetOpacityFillStroke(handle, fillAlpha, strokeAlpha);
        return this;
    }

    /**
     * Adds a highlight markup annotation over the specified area.
     *
     * @param x1         left coordinate in points
     * @param y1         bottom coordinate in points
     * @param x2         right coordinate in points
     * @param y2         top coordinate in points
     * @param color      the highlight {@link Color}
     * @param quadPoints flat array of quad-point coordinates defining the highlighted region
     * @return this page, for chaining
     */
    public Page addHighlight(double x1, double y1, double x2, double y2, Color color, double[] quadPoints) {
        FolioNative.pageAddHighlight(handle, x1, y1, x2, y2, color.r(), color.g(), color.b(), quadPoints);
        return this;
    }

    /**
     * Adds an underline markup annotation over the specified area.
     *
     * @param x1         left coordinate in points
     * @param y1         bottom coordinate in points
     * @param x2         right coordinate in points
     * @param y2         top coordinate in points
     * @param color      the underline {@link Color}
     * @param quadPoints flat array of quad-point coordinates
     * @return this page, for chaining
     */
    public Page addUnderlineAnnotation(double x1, double y1, double x2, double y2, Color color, double[] quadPoints) {
        FolioNative.pageAddUnderlineAnnotation(handle, x1, y1, x2, y2, color.r(), color.g(), color.b(), quadPoints);
        return this;
    }

    /**
     * Adds a squiggly underline markup annotation over the specified area.
     *
     * @param x1         left coordinate in points
     * @param y1         bottom coordinate in points
     * @param x2         right coordinate in points
     * @param y2         top coordinate in points
     * @param color      the squiggly line {@link Color}
     * @param quadPoints flat array of quad-point coordinates
     * @return this page, for chaining
     */
    public Page addSquiggly(double x1, double y1, double x2, double y2, Color color, double[] quadPoints) {
        FolioNative.pageAddSquiggly(handle, x1, y1, x2, y2, color.r(), color.g(), color.b(), quadPoints);
        return this;
    }

    /**
     * Adds a strikeout markup annotation over the specified area.
     *
     * @param x1         left coordinate in points
     * @param y1         bottom coordinate in points
     * @param x2         right coordinate in points
     * @param y2         top coordinate in points
     * @param color      the strikeout line {@link Color}
     * @param quadPoints flat array of quad-point coordinates
     * @return this page, for chaining
     */
    public Page addStrikeout(double x1, double y1, double x2, double y2, Color color, double[] quadPoints) {
        FolioNative.pageAddStrikeout(handle, x1, y1, x2, y2, color.r(), color.g(), color.b(), quadPoints);
        return this;
    }

    /**
     * Draws a straight line between two points on this page.
     *
     * @param x1    start x coordinate in points
     * @param y1    start y coordinate in points
     * @param x2    end x coordinate in points
     * @param y2    end y coordinate in points
     * @param width line width in points
     * @param r     stroke red channel
     * @param g     stroke green channel
     * @param b     stroke blue channel
     * @return this page, for chaining
     */
    public Page addLine(double x1, double y1, double x2, double y2, double width, double r, double g, double b) {
        FolioNative.pageAddLine(handle, x1, y1, x2, y2, width, r, g, b);
        return this;
    }

    /**
     * Draws a stroked rectangle on this page.
     *
     * @param x           left coordinate in points
     * @param y           bottom coordinate in points
     * @param w           width in points
     * @param h           height in points
     * @param strokeWidth stroke width in points
     * @param r           stroke red channel
     * @param g           stroke green channel
     * @param b           stroke blue channel
     * @return this page, for chaining
     */
    public Page addRect(double x, double y, double w, double h, double strokeWidth, double r, double g, double b) {
        FolioNative.pageAddRect(handle, x, y, w, h, strokeWidth, r, g, b);
        return this;
    }

    /**
     * Draws a filled rectangle on this page.
     *
     * @param x left coordinate in points
     * @param y bottom coordinate in points
     * @param w width in points
     * @param h height in points
     * @param r fill red channel
     * @param g fill green channel
     * @param b fill blue channel
     * @return this page, for chaining
     */
    public Page addRectFilled(double x, double y, double w, double h, double r, double g, double b) {
        FolioNative.pageAddRectFilled(handle, x, y, w, h, r, g, b);
        return this;
    }

    /**
     * Returns the native handle for this page.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle;
    }
}
