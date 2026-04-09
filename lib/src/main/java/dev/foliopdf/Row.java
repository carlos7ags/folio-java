package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * Represents a row within a {@link Table}. Instances are provided to the row builder
 * callback (e.g., {@link Table.Builder#row(java.util.function.Consumer)}) or obtained
 * via {@link Table.Builder#build()}.
 *
 * <p>Use {@link #addCell(String)} and its overloads to append cells, then chain
 * fluent setters on the returned {@link Cell}.
 */
public final class Row {

    private final long handle;
    private final Font font;
    private final double fontSize;

    Row(long handle, Font font, double fontSize) {
        this.handle = handle;
        this.font = font;
        this.fontSize = fontSize;
    }

    /**
     * Adds a text cell using the row's default font and size.
     *
     * @param text the cell text
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCell(String text) {
        long h = FolioNative.rowAddCell(handle, text, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to add cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Adds a text cell with a custom font and font size.
     *
     * @param text         the cell text
     * @param cellFont     the font for this cell
     * @param cellFontSize the font size in points for this cell
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCell(String text, Font cellFont, double cellFontSize) {
        long h = FolioNative.rowAddCell(handle, text, cellFont.handle(), cellFontSize);
        if (h == 0) throw new FolioException("Failed to add cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Adds a text cell that embeds the row's default font subset in the output.
     *
     * @param text the cell text
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCellEmbedded(String text) {
        long h = FolioNative.rowAddCellEmbedded(handle, text, font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to add embedded cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Adds a text cell with an embedded custom font subset.
     *
     * @param text         the cell text
     * @param cellFont     the font to embed for this cell
     * @param cellFontSize the font size in points for this cell
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCellEmbedded(String text, Font cellFont, double cellFontSize) {
        long h = FolioNative.rowAddCellEmbedded(handle, text, cellFont.handle(), cellFontSize);
        if (h == 0) throw new FolioException("Failed to add embedded cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Adds a cell whose content is rendered from a {@link Paragraph} element.
     *
     * @param paragraph the paragraph to place inside the cell
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCellElement(Paragraph paragraph) {
        long h = FolioNative.rowAddCellElement(handle, paragraph.handle());
        if (h == 0) throw new FolioException("Failed to add element cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Adds a cell whose content is rendered from an {@link Image} element.
     *
     * @param image the image to place inside the cell
     * @return the new {@link Cell}, for further styling
     * @throws FolioException if the native call fails
     */
    public Cell addCellElement(Image image) {
        long h = FolioNative.rowAddCellElement(handle, image.handle());
        if (h == 0) throw new FolioException("Failed to add element cell: " + FolioNative.lastError());
        return new Cell(h);
    }

    /**
     * Returns the native handle for this row.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle;
    }
}
