package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A multi-column layout container that places child elements into independently sized columns.
 *
 * <p>Content is assigned to a specific column by index, allowing side-by-side layout without
 * requiring a full grid setup.
 *
 * <pre>{@code
 * var columns = Columns.of(2)
 *     .gap(20)
 *     .widths(200, 300);
 * columns.add(0, Paragraph.of("Left column text", Font.helvetica(), 12));
 * columns.add(1, Paragraph.of("Right column text", Font.helvetica(), 12));
 * doc.add(columns);
 * }</pre>
 */
public final class Columns implements Element {

    private final HandleRef handle;

    private Columns(long handle) {
        this.handle = new HandleRef(handle, FolioNative::columnsFree);
    }

    /**
     * Creates a new column layout with the specified number of columns.
     *
     * @param cols the number of columns
     * @return a new {@code Columns} instance
     * @throws FolioException if the native columns container cannot be created
     */
    public static Columns of(int cols) {
        long h = FolioNative.columnsNew(cols);
        if (h == 0) throw new FolioException("Failed to create columns: " + FolioNative.lastError());
        return new Columns(h);
    }

    /**
     * Sets the gap between columns in points.
     *
     * @param gap the inter-column gap in points
     * @return this instance for chaining
     */
    public Columns gap(double gap) {
        FolioNative.columnsSetGap(handle.get(), gap);
        return this;
    }

    /**
     * Sets the width of each column in points.
     *
     * <p>The number of values should match the column count passed to {@link #of}.
     *
     * @param widths one width value per column, in points
     * @return this instance for chaining
     */
    public Columns widths(double... widths) {
        FolioNative.columnsSetWidths(handle.get(), widths);
        return this;
    }

    /**
     * Adds a paragraph to the specified column.
     *
     * @param colIndex the zero-based column index
     * @param paragraph the paragraph to add
     * @return this instance for chaining
     */
    public Columns add(int colIndex, Paragraph paragraph) {
        FolioNative.columnsAdd(handle.get(), colIndex, paragraph.handle());
        return this;
    }

    /**
     * Adds a heading to the specified column.
     *
     * @param colIndex the zero-based column index
     * @param heading  the heading to add
     * @return this instance for chaining
     */
    public Columns add(int colIndex, Heading heading) {
        FolioNative.columnsAdd(handle.get(), colIndex, heading.handle());
        return this;
    }

    /**
     * Adds a div to the specified column.
     *
     * @param colIndex the zero-based column index
     * @param div      the div to add
     * @return this instance for chaining
     */
    public Columns add(int colIndex, Div div) {
        FolioNative.columnsAdd(handle.get(), colIndex, div.handle());
        return this;
    }

    /**
     * Adds an image to the specified column.
     *
     * @param colIndex the zero-based column index
     * @param image    the image to add
     * @return this instance for chaining
     */
    public Columns add(int colIndex, Image image) {
        FolioNative.columnsAdd(handle.get(), colIndex, image.handle());
        return this;
    }

    /**
     * Adds a table to the specified column.
     *
     * @param colIndex the zero-based column index
     * @param table    the table to add
     * @return this instance for chaining
     */
    public Columns add(int colIndex, Table table) {
        FolioNative.columnsAdd(handle.get(), colIndex, table.handle());
        return this;
    }

    /**
     * Toggles balanced column fill. When balanced, the engine sequentially
     * fills columns to roughly equal heights instead of overflowing the
     * first column before starting the next. When disabled, content fills
     * each column to the available height in turn.
     *
     * <p>Balanced layout follows the model used by the CSS
     * {@code column-fill: balance} property (CSS Multi-column Layout, level 1).
     *
     * @param balanced {@code true} to enable balanced filling
     * @return this instance for chaining
     * @since 0.7.1
     */
    public Columns setBalanced(boolean balanced) {
        FolioNative.columnsSetBalanced(handle.get(), balanced);
        return this;
    }

    /**
     * Returns the native handle for this columns container.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
