package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A block-level container that can hold other layout elements such as
 * {@link Paragraph}, {@link Heading}, {@link Table}, {@link ListElement}, and {@link Image}.
 * Use it to apply shared padding, background, border, or sizing to a group of elements.
 *
 * <pre>{@code
 * doc.add(Div.of()
 *         .padding(12)
 *         .background(Color.LIGHT_GRAY)
 *         .border(1, Color.GRAY)
 *         .add(Paragraph.of("Note: prices exclude tax.")));
 * }</pre>
 */
public final class Div implements Element {

    private final HandleRef handle;

    private Div(long handle) {
        this.handle = new HandleRef(handle, FolioNative::divFree);
    }

    /**
     * Creates a new empty {@link Div} container.
     *
     * @return a new {@link Div} instance
     * @throws FolioException if the native call fails
     */
    public static Div of() {
        long h = FolioNative.divNew();
        if (h == 0) throw new FolioException("Failed to create div: " + FolioNative.lastError());
        return new Div(h);
    }

    /**
     * Appends a {@link Paragraph} to this div.
     *
     * @param paragraph the paragraph to add
     * @return this div, for chaining
     */
    public Div add(Paragraph paragraph) {
        FolioNative.divAdd(handle.get(), paragraph.handle());
        return this;
    }

    /**
     * Appends a {@link Heading} to this div.
     *
     * @param heading the heading to add
     * @return this div, for chaining
     */
    public Div add(Heading heading) {
        FolioNative.divAdd(handle.get(), heading.handle());
        return this;
    }

    /**
     * Appends a {@link Table} to this div.
     *
     * @param table the table to add
     * @return this div, for chaining
     */
    public Div add(Table table) {
        FolioNative.divAdd(handle.get(), table.handle());
        return this;
    }

    /**
     * Appends a {@link ListElement} to this div.
     *
     * @param list the list to add
     * @return this div, for chaining
     */
    public Div add(ListElement list) {
        FolioNative.divAdd(handle.get(), list.handle());
        return this;
    }

    /**
     * Appends an {@link Image} to this div.
     *
     * @param image the image to add
     * @return this div, for chaining
     */
    public Div add(Image image) {
        FolioNative.divAdd(handle.get(), image.handle());
        return this;
    }

    /** Adds any {@link Element} to this div. */
    public Div add(Element element) {
        FolioNative.divAdd(handle.get(), element.handle());
        return this;
    }

    /**
     * Sets individual padding values for each side of this div.
     *
     * @param top    top padding in points
     * @param right  right padding in points
     * @param bottom bottom padding in points
     * @param left   left padding in points
     * @return this div, for chaining
     */
    public Div padding(double top, double right, double bottom, double left) {
        FolioNative.divSetPadding(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Sets uniform padding on all four sides of this div.
     *
     * @param all padding in points applied to all sides
     * @return this div, for chaining
     */
    public Div padding(double all) {
        return padding(all, all, all, all);
    }

    /**
     * Sets the background fill color of this div.
     *
     * @param color the background {@link Color}
     * @return this div, for chaining
     */
    public Div background(Color color) {
        FolioNative.divSetBackground(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the border width and color for this div.
     *
     * @param width border line width in points
     * @param color the border {@link Color}
     * @return this div, for chaining
     */
    public Div border(double width, Color color) {
        FolioNative.divSetBorder(handle.get(), width, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the explicit width of this div in points.
     *
     * @param pts width in points
     * @return this div, for chaining
     */
    public Div width(double pts) {
        FolioNative.divSetWidth(handle.get(), pts);
        return this;
    }

    /**
     * Sets the minimum height of this div in points.
     *
     * @param pts minimum height in points
     * @return this div, for chaining
     */
    public Div minHeight(double pts) {
        FolioNative.divSetMinHeight(handle.get(), pts);
        return this;
    }

    /**
     * Sets the amount of vertical space to add before this div.
     *
     * @param pts space before in points
     * @return this div, for chaining
     */
    public Div spaceBefore(double pts) {
        FolioNative.divSetSpaceBefore(handle.get(), pts);
        return this;
    }

    /**
     * Sets the amount of vertical space to add after this div.
     *
     * @param pts space after in points
     * @return this div, for chaining
     */
    public Div spaceAfter(double pts) {
        FolioNative.divSetSpaceAfter(handle.get(), pts);
        return this;
    }

    /**
     * Sets the corner radius for rounded borders on this div.
     *
     * @param radius border radius in points
     * @return this div, for chaining
     */
    public Div borderRadius(double radius) {
        FolioNative.divSetBorderRadius(handle.get(), radius);
        return this;
    }

    /**
     * Sets the opacity of this div and its contents.
     *
     * @param opacity opacity in the range {@code [0.0, 1.0]}
     * @return this div, for chaining
     */
    public Div opacity(double opacity) {
        FolioNative.divSetOpacity(handle.get(), opacity);
        return this;
    }

    /**
     * Sets the overflow behaviour when content exceeds this div's bounds.
     *
     * @param mode overflow mode string (e.g., {@code "hidden"})
     * @return this div, for chaining
     */
    public Div overflow(String mode) {
        FolioNative.divSetOverflow(handle.get(), mode);
        return this;
    }

    /**
     * Overrides the PDF/UA structure tag for this div (e.g., "Note", "Aside").
     *
     * @param tag the PDF structure tag name
     * @return this div, for chaining
     */
    public Div tag(String tag) {
        FolioNative.divSetTag(handle.get(), tag);
        return this;
    }

    /**
     * Sets the maximum width of this div in points.
     *
     * @param pts maximum width in points
     * @return this div, for chaining
     */
    public Div maxWidth(double pts) {
        FolioNative.divSetMaxWidth(handle.get(), pts);
        return this;
    }

    /**
     * Sets the minimum width of this div in points.
     *
     * @param pts minimum width in points
     * @return this div, for chaining
     */
    public Div minWidth(double pts) {
        FolioNative.divSetMinWidth(handle.get(), pts);
        return this;
    }

    /**
     * Sets the maximum height of this div in points.
     *
     * @param pts maximum height in points
     * @return this div, for chaining
     */
    public Div maxHeight(double pts) {
        FolioNative.divSetMaxHeight(handle.get(), pts);
        return this;
    }

    /**
     * Adds a drop shadow to this div.
     *
     * @param offsetX horizontal shadow offset in points
     * @param offsetY vertical shadow offset in points
     * @param blur    blur radius in points
     * @param spread  spread radius in points
     * @param color   shadow {@link Color}
     * @return this div, for chaining
     */
    public Div boxShadow(double offsetX, double offsetY, double blur, double spread, Color color) {
        FolioNative.divSetBoxShadow(handle.get(), offsetX, offsetY, blur, spread, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets this div's width as a percentage of its containing block.
     *
     * @param pct percent width (e.g., {@code 50.0} for half-width)
     * @return this div, for chaining
     */
    public Div setWidthPercent(double pct) {
        FolioNative.divSetWidthPercent(handle.get(), pct);
        return this;
    }

    /**
     * Sets a fixed width/height aspect ratio for this div.
     *
     * @param ratio width divided by height
     * @return this div, for chaining
     */
    public Div setAspectRatio(double ratio) {
        FolioNative.divSetAspectRatio(handle.get(), ratio);
        return this;
    }

    /**
     * Requests that the layout engine keep this div on a single page rather
     * than splitting it across a page break.
     *
     * @param enabled whether to keep the div together
     * @return this div, for chaining
     */
    public Div setKeepTogether(boolean enabled) {
        FolioNative.divSetKeepTogether(handle.get(), enabled);
        return this;
    }

    /**
     * Sets individual border radii for each corner.
     *
     * @param tl top-left radius in points
     * @param tr top-right radius in points
     * @param br bottom-right radius in points
     * @param bl bottom-left radius in points
     * @return this div, for chaining
     */
    public Div setBorderRadius(double tl, double tr, double br, double bl) {
        FolioNative.divSetBorderRadiusPerCorner(handle.get(), tl, tr, br, bl);
        return this;
    }

    /**
     * Centers this div horizontally within its container.
     *
     * @param enabled whether to apply horizontal centering
     * @return this div, for chaining
     */
    public Div setHCenter(boolean enabled) {
        FolioNative.divSetHCenter(handle.get(), enabled);
        return this;
    }

    /**
     * Right-aligns this div within its container.
     *
     * @param enabled whether to right-align
     * @return this div, for chaining
     */
    public Div setHRight(boolean enabled) {
        FolioNative.divSetHRight(handle.get(), enabled);
        return this;
    }

    /**
     * Sets the CSS-style {@code clear} property for this div.
     *
     * @param value one of {@code "left"}, {@code "right"}, or {@code "both"}
     * @return this div, for chaining
     */
    public Div setClear(String value) {
        FolioNative.divSetClear(handle.get(), value);
        return this;
    }

    /**
     * Draws an outline around this div that sits outside the border box.
     *
     * @param width  outline width in points
     * @param style  outline style (e.g., {@code "solid"}, {@code "dashed"})
     * @param r      outline red channel
     * @param g      outline green channel
     * @param b      outline blue channel
     * @param offset outline offset from the border box
     * @return this div, for chaining
     */
    public Div setOutline(double width, String style, double r, double g, double b, double offset) {
        FolioNative.divSetOutline(handle.get(), width, style, r, g, b, offset);
        return this;
    }

    /**
     * Adds a box shadow to this div. Multiple shadows may be layered with
     * repeated calls.
     *
     * @param ox     horizontal offset in points
     * @param oy     vertical offset in points
     * @param blur   blur radius in points
     * @param spread spread radius in points
     * @param r      shadow red channel
     * @param g      shadow green channel
     * @param b      shadow blue channel
     * @return this div, for chaining
     */
    public Div addBoxShadow(double ox, double oy, double blur, double spread, double r, double g, double b) {
        FolioNative.divAddBoxShadow(handle.get(), ox, oy, blur, spread, r, g, b);
        return this;
    }

    /**
     * Returns the native handle for this div.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }
}
