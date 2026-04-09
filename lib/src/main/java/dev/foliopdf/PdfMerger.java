package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Merges multiple PDF documents into a single output. Create from
 * {@link PdfReader} handles or file paths, then save the result.
 *
 * <pre>{@code
 * try (var r1 = PdfReader.open("report-q3.pdf");
 *      var r2 = PdfReader.open("report-q4.pdf");
 *      var merged = PdfMerger.merge(r1, r2)) {
 *     merged.setInfo("Annual Report", "ACME Corp");
 *     merged.save("annual.pdf");
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class PdfMerger implements AutoCloseable {

    private final HandleRef handle;

    private PdfMerger(long handle) {
        this.handle = new HandleRef(handle, FolioNative::mergeFree);
    }

    /**
     * Merges multiple {@link PdfReader} instances into a single document.
     *
     * @param readers the PDF readers to merge (in order)
     * @return a new {@code PdfMerger} with the combined pages
     * @throws FolioException if the merge fails
     */
    public static PdfMerger merge(PdfReader... readers) {
        long[] handles = new long[readers.length];
        for (int i = 0; i < readers.length; i++) {
            handles[i] = readers[i].readerHandle();
        }
        long h = FolioNative.readerMerge(handles);
        if (h == 0) throw new FolioException("Failed to merge PDFs: " + FolioNative.lastError());
        return new PdfMerger(h);
    }

    /**
     * Merges PDF files by path and saves the result directly.
     *
     * <pre>{@code
     * PdfMerger.merge("merged.pdf", "a.pdf", "b.pdf", "c.pdf");
     * }</pre>
     *
     * @param outputPath the destination file path for the merged PDF
     * @param inputPaths the file paths of the PDFs to merge (in order)
     * @throws FolioException if any file cannot be read or the merge fails
     */
    public static void merge(String outputPath, String... inputPaths) {
        try (var merger = mergeFiles(inputPaths)) {
            merger.save(outputPath);
        }
    }

    /**
     * Merges PDF files by path and saves the result directly.
     *
     * @param outputPath the destination file path for the merged PDF
     * @param inputPaths the file paths of the PDFs to merge (in order)
     * @throws FolioException if any file cannot be read or the merge fails
     */
    public static void merge(java.nio.file.Path outputPath, java.nio.file.Path... inputPaths) {
        String[] paths = new String[inputPaths.length];
        for (int i = 0; i < inputPaths.length; i++) {
            paths[i] = inputPaths[i].toString();
        }
        merge(outputPath.toString(), paths);
    }

    /**
     * Merges PDF files by path into a single document.
     *
     * @param paths the file paths of the PDFs to merge (in order)
     * @return a new {@code PdfMerger} with the combined pages
     * @throws FolioException if any file cannot be read or the merge fails
     */
    public static PdfMerger mergeFiles(String... paths) {
        long h = FolioNative.mergeFiles(paths);
        if (h == 0) throw new FolioException("Failed to merge PDF files: " + FolioNative.lastError());
        return new PdfMerger(h);
    }

    /**
     * Sets the title and author metadata on the merged document.
     *
     * @param title  the document title
     * @param author the document author
     * @return this merger, for chaining
     */
    public PdfMerger setInfo(String title, String author) {
        FolioNative.mergeSetInfo(handle.get(), title, author);
        return this;
    }

    /**
     * Appends a blank page with the given dimensions.
     *
     * @param width  page width in points
     * @param height page height in points
     * @return this merger, for chaining
     */
    public PdfMerger addBlankPage(double width, double height) {
        FolioNative.mergeAddBlankPage(handle.get(), width, height);
        return this;
    }

    /**
     * Appends a page with text at a specific position.
     *
     * @param width    page width in points
     * @param height   page height in points
     * @param text     text to place on the page
     * @param font     the font to use
     * @param fontSize font size in points
     * @param x        x coordinate
     * @param y        y coordinate
     * @return this merger, for chaining
     */
    public PdfMerger addPageWithText(double width, double height, String text, Font font, double fontSize, double x, double y) {
        FolioNative.mergeAddPageWithText(handle.get(), width, height, text, font.handle(), fontSize, x, y);
        return this;
    }

    /**
     * Saves the merged document to a file.
     *
     * @param path the output file path
     * @throws FolioException if the save fails
     */
    public void save(String path) {
        FolioNative.mergeSave(handle.get(), path);
    }

    /**
     * Renders the merged document to an in-memory byte array.
     *
     * @return the PDF bytes
     * @throws FolioException if the write fails
     */
    /** Returns the number of pages in the merged document. */
    public int pageCount() {
        return FolioNative.mergePageCount(handle.get());
    }

    /** Removes the page at the given zero-based index. */
    public PdfMerger removePage(int index) {
        FolioNative.mergeRemovePage(handle.get(), index);
        return this;
    }

    /** Rotates the page at the given index by the specified degrees (90, 180, 270). */
    public PdfMerger rotatePage(int index, int degrees) {
        FolioNative.mergeRotatePage(handle.get(), index, degrees);
        return this;
    }

    /**
     * Reorders pages. The array specifies the new order by old page indices.
     * For example, {@code {2, 0, 1}} moves page 2 to first position.
     */
    public PdfMerger reorderPages(int... order) {
        FolioNative.mergeReorderPages(handle.get(), order);
        return this;
    }

    /**
     * Flattens all interactive form fields into static content.
     * After flattening, fields are no longer editable.
     */
    public PdfMerger flattenForms() {
        FolioNative.mergeFlattenForms(handle.get());
        return this;
    }

    /** Crops the page at the given index to the specified rectangle. */
    public PdfMerger cropPage(int index, double x1, double y1, double x2, double y2) {
        FolioNative.mergeCropPage(handle.get(), index, x1, y1, x2, y2);
        return this;
    }

    public byte[] writeToBuffer() {
        long buf = FolioNative.mergeWriteToBuffer(handle.get());
        if (buf == 0) throw new FolioException("Failed to write merged PDF to buffer: " + FolioNative.lastError());
        try {
            int len = FolioNative.bufferLen(buf);
            MemorySegment data = FolioNative.bufferData(buf);
            return data.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
        } finally {
            FolioNative.bufferFree(buf);
        }
    }

    @Override
    public void close() {
        handle.close();
    }
}
