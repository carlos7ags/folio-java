package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A layout element that floats its content to the left or right side of the page, allowing
 * surrounding content to wrap around it.
 *
 * <pre>{@code
 * var logo = Image.load("logo.png").size(80, 80);
 * doc.add(FloatElement.of(FloatSide.LEFT, logo).margin(8));
 * doc.add(Paragraph.of("This text wraps around the floated image.", Font.helvetica(), 12));
 * }</pre>
 */
public final class FloatElement implements Element {

    private final HandleRef handle;

    private FloatElement(long handle) {
        this.handle = new HandleRef(handle, FolioNative::floatFree);
    }

    /**
     * Creates a float element that positions a paragraph on the given side.
     *
     * @param side      the side to float to
     * @param paragraph the paragraph to float
     * @return a new {@code FloatElement} instance
     * @throws FolioException if the native float element cannot be created
     */
    public static FloatElement of(FloatSide side, Paragraph paragraph) {
        return create(side, paragraph.handle());
    }

    /**
     * Creates a float element that positions an image on the given side.
     *
     * @param side  the side to float to
     * @param image the image to float
     * @return a new {@code FloatElement} instance
     * @throws FolioException if the native float element cannot be created
     */
    public static FloatElement of(FloatSide side, Image image) {
        return create(side, image.handle());
    }

    /**
     * Creates a float element that positions a div on the given side.
     *
     * @param side the side to float to
     * @param div  the div to float
     * @return a new {@code FloatElement} instance
     * @throws FolioException if the native float element cannot be created
     */
    public static FloatElement of(FloatSide side, Div div) {
        return create(side, div.handle());
    }

    /**
     * Creates a float element that positions a table on the given side.
     *
     * @param side  the side to float to
     * @param table the table to float
     * @return a new {@code FloatElement} instance
     * @throws FolioException if the native float element cannot be created
     */
    public static FloatElement of(FloatSide side, Table table) {
        return create(side, table.handle());
    }

    private static FloatElement create(FloatSide side, long elementHandle) {
        long h = FolioNative.floatNew(side.value(), elementHandle);
        if (h == 0) throw new FolioException("Failed to create float element: " + FolioNative.lastError());
        return new FloatElement(h);
    }

    /**
     * Sets the margin around the floated element, separating it from surrounding content.
     *
     * @param margin the margin in points
     * @return this instance for chaining
     */
    public FloatElement margin(double margin) {
        FolioNative.floatSetMargin(handle.get(), margin);
        return this;
    }

    /**
     * Returns the native handle for this float element.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
