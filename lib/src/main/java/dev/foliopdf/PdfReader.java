package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Opens an existing PDF for inspection — reading metadata, page dimensions, and
 * extracting text. Create instances with {@link #open(String)} or {@link #parse(byte[])}.
 *
 * <pre>{@code
 * try (var reader = PdfReader.open("report.pdf")) {
 *     System.out.println("Pages: " + reader.pageCount());
 *     System.out.println("Title: " + reader.title());
 *     System.out.println("Page 1 text: " + reader.extractText(0));
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to ensure the
 * underlying native handle is freed.
 */
public final class PdfReader implements AutoCloseable {

    private final HandleRef handle;

    private PdfReader(long handle) {
        this.handle = new HandleRef(handle, FolioNative::readerFree);
    }

    /**
     * Opens a PDF file from disk.
     *
     * @param path absolute path to the PDF file
     * @return a new {@link PdfReader} for the file
     * @throws FolioIOException if the file cannot be read (I/O error)
     * @throws FolioException if the file is not a valid PDF or another native error occurs
     */
    public static PdfReader open(String path) {
        long h = FolioNative.readerOpen(path);
        if (h == 0) throw new FolioIOException("Failed to open PDF: " + path + ": " + FolioNative.lastError());
        return new PdfReader(h);
    }

    /**
     * Opens a PDF file from disk using a {@link java.nio.file.Path}.
     *
     * @param path path to the PDF file
     * @return a new {@link PdfReader} for the file
     * @throws FolioIOException if the file cannot be read (I/O error)
     * @throws FolioException if the file is not a valid PDF or another native error occurs
     */
    public static PdfReader open(java.nio.file.Path path) {
        return open(path.toString());
    }

    /**
     * Parses a PDF from raw bytes.
     *
     * @param data the raw PDF bytes
     * @return a new {@link PdfReader} for the in-memory PDF
     * @throws FolioException if the bytes are not a valid PDF
     */
    public static PdfReader parse(byte[] data) {
        long h = FolioNative.readerParse(data);
        if (h == 0) throw new FolioException("Failed to parse PDF from bytes: " + FolioNative.lastError());
        return new PdfReader(h);
    }

    /**
     * Reads a PDF from an {@link java.io.InputStream} by consuming all bytes.
     *
     * @param in the input stream to read from (fully consumed)
     * @return a new {@link PdfReader} for the in-memory PDF
     * @throws java.io.IOException if an I/O error occurs while reading the stream
     * @throws FolioException if the bytes are not a valid PDF
     */
    public static PdfReader from(java.io.InputStream in) throws java.io.IOException {
        return parse(in.readAllBytes());
    }

    /**
     * Returns the total number of pages in the PDF.
     *
     * @return page count
     */
    public int pageCount() {
        return FolioNative.readerPageCount(handle.get());
    }

    /**
     * Extracts the plain text content from the specified page.
     *
     * @param pageIndex zero-based page index
     * @return the extracted text, or an empty string if the page has no text
     */
    public String extractText(int pageIndex) {
        long buf = FolioNative.readerExtractText(handle.get(), pageIndex);
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns the document title from PDF metadata, or an empty string if not set.
     *
     * @return the title metadata value
     */
    public String title() {
        long buf = FolioNative.readerInfoTitle(handle.get());
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns the document author from PDF metadata, or an empty string if not set.
     *
     * @return the author metadata value
     */
    public String author() {
        long buf = FolioNative.readerInfoAuthor(handle.get());
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns the width of the specified page in points.
     *
     * @param pageIndex zero-based page index
     * @return page width in points
     */
    public double pageWidth(int pageIndex) {
        return FolioNative.readerPageWidth(handle.get(), pageIndex);
    }

    /**
     * Returns the height of the specified page in points.
     *
     * @param pageIndex zero-based page index
     * @return page height in points
     */
    public double pageHeight(int pageIndex) {
        return FolioNative.readerPageHeight(handle.get(), pageIndex);
    }

    /**
     * Returns the PDF version string (e.g., {@code "1.7"}).
     *
     * @return the PDF version
     */
    public String pdfVersion() {
        long buf = FolioNative.readerVersion(handle.get());
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns structured text spans (with positions and fonts) from a page.
     * The result is a JSON string describing each span.
     *
     * @param pageIndex zero-based page index
     * @return JSON string of text spans, or null
     */
    public String textSpans(int pageIndex) {
        long buf = FolioNative.readerTextSpans(handle.get(), pageIndex);
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns image metadata from a page.
     * The result is a JSON string describing each embedded image.
     *
     * @param pageIndex zero-based page index
     * @return JSON string of image data, or null
     */
    public String images(int pageIndex) {
        long buf = FolioNative.readerImages(handle.get(), pageIndex);
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns the PDF/UA structure tree as JSON. Returns null if the
     * document is not tagged.
     *
     * @return JSON string of the tag structure tree, or null
     */
    public String structureTree() {
        long buf = FolioNative.readerStructureTree(handle.get());
        return FolioNative.bufferToString(buf);
    }

    /**
     * Returns vector path data from a page.
     * The result is a JSON string describing each drawing path.
     *
     * @param pageIndex zero-based page index
     * @return JSON string of path data, or null
     */
    public String paths(int pageIndex) {
        long buf = FolioNative.readerPaths(handle.get(), pageIndex);
        return FolioNative.bufferToString(buf);
    }

    /**
     * Extracts text from all pages, concatenated with newlines.
     *
     * @return the combined text from every page
     */
    public String extractAllText() {
        StringBuilder sb = new StringBuilder();
        int pages = pageCount();
        for (int i = 0; i < pages; i++) {
            if (i > 0) sb.append('\n');
            String text = extractText(i);
            if (text != null) sb.append(text);
        }
        return sb.toString();
    }

    long readerHandle() {
        return handle.get();
    }

    /**
     * Frees the underlying native reader handle. Called automatically by
     * try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
    }
}
