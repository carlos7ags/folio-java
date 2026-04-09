package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * Represents a single cell within a table {@link Row}. Instances are returned
 * by {@link Row#addCell(String)} and its overloads.
 *
 * <p>Chain fluent setters to control alignment, padding, background, spanning, and borders.
 */
public final class Cell {

    private final long handle;

    Cell(long handle) {
        this.handle = handle;
    }

    /**
     * Sets the horizontal text alignment within this cell.
     *
     * @param align the desired {@link Align} value
     * @return this cell, for chaining
     */
    public Cell align(Align align) {
        FolioNative.cellSetAlign(handle, align.value());
        return this;
    }

    /**
     * Sets uniform padding on all four sides of this cell.
     *
     * @param padding padding in points
     * @return this cell, for chaining
     */
    public Cell padding(double padding) {
        FolioNative.cellSetPadding(handle, padding);
        return this;
    }

    /**
     * Sets the background fill color of this cell.
     *
     * @param color the background {@link Color}
     * @return this cell, for chaining
     */
    public Cell background(Color color) {
        FolioNative.cellSetBackground(handle, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the number of columns this cell spans.
     *
     * @param n number of columns to span
     * @return this cell, for chaining
     */
    public Cell colspan(int n) {
        FolioNative.cellSetColspan(handle, n);
        return this;
    }

    /**
     * Sets the number of rows this cell spans.
     *
     * @param n number of rows to span
     * @return this cell, for chaining
     */
    public Cell rowspan(int n) {
        FolioNative.cellSetRowspan(handle, n);
        return this;
    }

    /**
     * Sets individual padding values for each side of this cell.
     *
     * @param top    top padding in points
     * @param right  right padding in points
     * @param bottom bottom padding in points
     * @param left   left padding in points
     * @return this cell, for chaining
     */
    public Cell paddingSides(double top, double right, double bottom, double left) {
        FolioNative.cellSetPaddingSides(handle, top, right, bottom, left);
        return this;
    }

    /**
     * Sets the vertical alignment of content within this cell.
     *
     * @param valign the desired {@link VAlign} value
     * @return this cell, for chaining
     */
    public Cell valign(VAlign valign) {
        FolioNative.cellSetValign(handle, valign.value());
        return this;
    }

    /**
     * Sets the border width and color for this cell.
     *
     * @param width the border line width in points
     * @param color the border {@link Color}
     * @return this cell, for chaining
     */
    public Cell border(double width, Color color) {
        FolioNative.cellSetBorder(handle, width, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Provides a hint for the preferred width of this cell in points.
     *
     * @param pts the preferred width in points
     * @return this cell, for chaining
     */
    /**
     * Sets individual borders on each side of this cell with independent width and color.
     *
     * @param top    border specification for the top edge
     * @param right  border specification for the right edge
     * @param bottom border specification for the bottom edge
     * @param left   border specification for the left edge
     * @return this cell, for chaining
     */
    public Cell borders(double topW, Color top, double rightW, Color right,
                        double bottomW, Color bottom, double leftW, Color left) {
        FolioNative.cellSetBorders(handle,
            topW, top.r(), top.g(), top.b(),
            rightW, right.r(), right.g(), right.b(),
            bottomW, bottom.r(), bottom.g(), bottom.b(),
            leftW, left.r(), left.g(), left.b());
        return this;
    }

    public Cell widthHint(double pts) {
        FolioNative.cellSetWidthHint(handle, pts);
        return this;
    }

    /**
     * Sets a uniform corner radius for this cell.
     *
     * @param radius border radius in points
     * @return this cell, for chaining
     */
    public Cell setBorderRadius(double radius) {
        FolioNative.cellSetBorderRadius(handle, radius);
        return this;
    }

    /**
     * Sets individual corner radii for this cell.
     *
     * @param tl top-left radius in points
     * @param tr top-right radius in points
     * @param br bottom-right radius in points
     * @param bl bottom-left radius in points
     * @return this cell, for chaining
     */
    public Cell setBorderRadius(double tl, double tr, double br, double bl) {
        FolioNative.cellSetBorderRadiusPerCorner(handle, tl, tr, br, bl);
        return this;
    }

    /**
     * Returns the native handle for this cell.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle;
    }
}
