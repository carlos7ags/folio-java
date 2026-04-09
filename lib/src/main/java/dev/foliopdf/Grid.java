package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A CSS grid-style layout container that arranges child elements into explicit rows and columns.
 *
 * <p>Children are added in order and can be explicitly placed using {@link #placement}. Track
 * sizes use {@link GridTrackType} constants to specify pixels, percentages, fractions, or auto
 * sizing.
 *
 * <pre>{@code
 * var grid = Grid.of()
 *     .templateColumns(
 *         new GridTrackType[]{GridTrackType.FR, GridTrackType.FR},
 *         new double[]{1, 2})
 *     .gap(8, 12);
 * grid.addChild(Paragraph.of("Left", Font.helvetica(), 12));
 * grid.addChild(Paragraph.of("Right (wider)", Font.helvetica(), 12));
 * doc.add(grid);
 * }</pre>
 */
public final class Grid implements Element {

    private final HandleRef handle;

    private Grid(long handle) {
        this.handle = new HandleRef(handle, FolioNative::gridFree);
    }

    /**
     * Creates a new, empty grid container with default settings.
     *
     * @return a new {@code Grid} instance
     * @throws FolioException if the native grid cannot be created
     */
    public static Grid of() {
        long h = FolioNative.gridNew();
        if (h == 0) throw new FolioException("Failed to create grid: " + FolioNative.lastError());
        return new Grid(h);
    }

    /**
     * Adds a paragraph as the next child in this grid.
     *
     * @param paragraph the paragraph to add
     * @return this instance for chaining
     */
    public Grid addChild(Paragraph paragraph) {
        FolioNative.gridAddChild(handle.get(), paragraph.handle());
        return this;
    }

    /**
     * Adds a heading as the next child in this grid.
     *
     * @param heading the heading to add
     * @return this instance for chaining
     */
    public Grid addChild(Heading heading) {
        FolioNative.gridAddChild(handle.get(), heading.handle());
        return this;
    }

    /**
     * Adds a div as the next child in this grid.
     *
     * @param div the div to add
     * @return this instance for chaining
     */
    public Grid addChild(Div div) {
        FolioNative.gridAddChild(handle.get(), div.handle());
        return this;
    }

    /**
     * Adds an image as the next child in this grid.
     *
     * @param image the image to add
     * @return this instance for chaining
     */
    public Grid addChild(Image image) {
        FolioNative.gridAddChild(handle.get(), image.handle());
        return this;
    }

    /**
     * Adds a table as the next child in this grid.
     *
     * @param table the table to add
     * @return this instance for chaining
     */
    public Grid addChild(Table table) {
        FolioNative.gridAddChild(handle.get(), table.handle());
        return this;
    }

    /** Adds any {@link Element} as the next child in this grid. */
    public Grid addChild(Element element) {
        FolioNative.gridAddChild(handle.get(), element.handle());
        return this;
    }

    /**
     * Defines the explicit column track sizes for the grid.
     *
     * <p>Each entry in {@code types} corresponds to a track type ({@link GridTrackType}) and the
     * matching entry in {@code values} provides the numeric size for that track.
     *
     * @param types  array of track type specifiers
     * @param values array of track size values
     * @return this instance for chaining
     */
    public Grid templateColumns(GridTrackType[] types, double[] values) {
        int[] typeValues = new int[types.length];
        for (int i = 0; i < types.length; i++) typeValues[i] = types[i].value();
        FolioNative.gridSetTemplateColumns(handle.get(), typeValues, values);
        return this;
    }

    /**
     * Defines the explicit row track sizes for the grid.
     *
     * <p>Each entry in {@code types} corresponds to a track type and the matching entry in
     * {@code values} provides the numeric size.
     *
     * @param types  array of track type specifiers
     * @param values array of track size values
     * @return this instance for chaining
     */
    public Grid templateRows(GridTrackType[] types, double[] values) {
        int[] typeValues = new int[types.length];
        for (int i = 0; i < types.length; i++) typeValues[i] = types[i].value();
        FolioNative.gridSetTemplateRows(handle.get(), typeValues, values);
        return this;
    }

    /**
     * Sets the implicit row track sizes used for rows created outside the explicit template.
     *
     * @param types  array of track type specifiers
     * @param values array of track size values
     * @return this instance for chaining
     */
    public Grid autoRows(GridTrackType[] types, double[] values) {
        int[] typeValues = new int[types.length];
        for (int i = 0; i < types.length; i++) typeValues[i] = types[i].value();
        FolioNative.gridSetAutoRows(handle.get(), typeValues, values);
        return this;
    }

    /**
     * Sets the row and column gaps between grid cells.
     *
     * @param rowGap the gap between rows in points
     * @param colGap the gap between columns in points
     * @return this instance for chaining
     */
    public Grid gap(double rowGap, double colGap) {
        FolioNative.gridSetGap(handle.get(), rowGap, colGap);
        return this;
    }

    /**
     * Explicitly places a child element into a specific grid area.
     *
     * <p>Grid lines are 1-based. Use 0 for any boundary to indicate auto placement.
     *
     * @param childIndex the zero-based index of the child (in the order it was added)
     * @param colStart   the starting column line (1-based)
     * @param colEnd     the ending column line (exclusive, 1-based)
     * @param rowStart   the starting row line (1-based)
     * @param rowEnd     the ending row line (exclusive, 1-based)
     * @return this instance for chaining
     */
    public Grid placement(int childIndex, int colStart, int colEnd, int rowStart, int rowEnd) {
        FolioNative.gridSetPlacement(handle.get(), childIndex, colStart, colEnd, rowStart, rowEnd);
        return this;
    }

    /**
     * Sets uniform padding on all sides of this grid container.
     *
     * @param padding the padding in points
     * @return this instance for chaining
     */
    public Grid padding(double padding) {
        FolioNative.gridSetPadding(handle.get(), padding);
        return this;
    }

    /**
     * Sets the background color of this grid container.
     *
     * @param color the RGB background color
     * @return this instance for chaining
     */
    public Grid background(Color color) {
        FolioNative.gridSetBackground(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets the default horizontal alignment of items within their grid cells.
     *
     * @param align the horizontal alignment
     * @return this instance for chaining
     */
    public Grid justifyItems(Align align) {
        FolioNative.gridSetJustifyItems(handle.get(), align.value());
        return this;
    }

    /**
     * Sets the default vertical alignment of items within their grid cells.
     *
     * @param align the vertical alignment
     * @return this instance for chaining
     */
    public Grid alignItems(AlignItems align) {
        FolioNative.gridSetAlignItems(handle.get(), align.value());
        return this;
    }

    /**
     * Sets how the grid tracks are distributed along the inline (column) axis.
     *
     * @param justify the justify-content strategy
     * @return this instance for chaining
     */
    public Grid justifyContent(JustifyContent justify) {
        FolioNative.gridSetJustifyContent(handle.get(), justify.value());
        return this;
    }

    /**
     * Sets how the grid tracks are distributed along the block (row) axis.
     *
     * @param align the align-content strategy
     * @return this instance for chaining
     */
    public Grid alignContent(AlignItems align) {
        FolioNative.gridSetAlignContent(handle.get(), align.value());
        return this;
    }

    /**
     * Sets extra vertical space before this grid container in the document flow.
     *
     * @param pts space in points
     * @return this instance for chaining
     */
    public Grid spaceBefore(double pts) {
        FolioNative.gridSetSpaceBefore(handle.get(), pts);
        return this;
    }

    /**
     * Sets extra vertical space after this grid container in the document flow.
     *
     * @param pts space in points
     * @return this instance for chaining
     */
    public Grid spaceAfter(double pts) {
        FolioNative.gridSetSpaceAfter(handle.get(), pts);
        return this;
    }

    /**
     * Sets a uniform border around this grid container.
     *
     * @param width border width in points
     * @param r     border red channel
     * @param g     border green channel
     * @param b     border blue channel
     * @return this instance for chaining
     */
    public Grid setBorder(double width, double r, double g, double b) {
        FolioNative.gridSetBorder(handle.get(), width, r, g, b);
        return this;
    }

    /**
     * Convenience overload accepting a {@link Color} for the border.
     *
     * @param width border width in points
     * @param color border color
     * @return this instance for chaining
     */
    public Grid setBorder(double width, Color color) {
        return setBorder(width, color.r(), color.g(), color.b());
    }

    /**
     * Sets individual borders for each edge of the grid container.
     *
     * @param topW    top border width
     * @param topR    top border red channel
     * @param topG    top border green channel
     * @param topB    top border blue channel
     * @param rightW  right border width
     * @param rightR  right border red channel
     * @param rightG  right border green channel
     * @param rightB  right border blue channel
     * @param bottomW bottom border width
     * @param bottomR bottom border red channel
     * @param bottomG bottom border green channel
     * @param bottomB bottom border blue channel
     * @param leftW   left border width
     * @param leftR   left border red channel
     * @param leftG   left border green channel
     * @param leftB   left border blue channel
     * @return this instance for chaining
     */
    public Grid setBorders(double topW, double topR, double topG, double topB,
                           double rightW, double rightR, double rightG, double rightB,
                           double bottomW, double bottomR, double bottomG, double bottomB,
                           double leftW, double leftR, double leftG, double leftB) {
        FolioNative.gridSetBorders(handle.get(),
            topW, topR, topG, topB,
            rightW, rightR, rightG, rightB,
            bottomW, bottomR, bottomG, bottomB,
            leftW, leftR, leftG, leftB);
        return this;
    }

    /**
     * Defines CSS-style named grid areas. Each row string lists area names
     * separated by whitespace (e.g., {@code "header header"}, {@code "nav main"}).
     *
     * @param rows the template area rows
     * @return this instance for chaining
     */
    public Grid setTemplateAreas(String... rows) {
        int[] cols = new int[rows.length];
        for (int i = 0; i < rows.length; i++) {
            String r = rows[i].trim();
            if (r.isEmpty()) {
                cols[i] = 0;
            } else {
                cols[i] = r.split("\\s+").length;
            }
        }
        FolioNative.gridSetTemplateAreas(handle.get(), rows, cols);
        return this;
    }

    /**
     * Returns the native handle for this grid container.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
