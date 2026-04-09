package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

import java.lang.foreign.MemorySegment;

/**
 * Utility class for converting HTML content to PDF. All methods are static;
 * this class cannot be instantiated.
 *
 * <pre>{@code
 * // Write directly to a file
 * HtmlConverter.toPdf("<h1>Invoice</h1><p>Due: $1,200</p>", "invoice.pdf");
 *
 * // Obtain raw PDF bytes
 * byte[] bytes = HtmlConverter.toBuffer("<p>Hello</p>");
 *
 * // Convert to a Document for further manipulation
 * try (var doc = HtmlConverter.toDocument("<p>Hello</p>")) {
 *     doc.save("out.pdf");
 * }
 * }</pre>
 */
public final class HtmlConverter {

    private HtmlConverter() {}

    /**
     * Converts an HTML string to a PDF file and writes it to the given path.
     *
     * @param html       the HTML content to render
     * @param outputPath the destination file path for the generated PDF
     * @throws FolioIOException if the file cannot be written (I/O error)
     * @throws FolioException if conversion fails for other reasons
     */
    public static void toPdf(String html, String outputPath) {
        FolioNative.htmlToPdf(html, outputPath);
    }

    /**
     * Converts an HTML string to a PDF file and writes it to the given {@link java.nio.file.Path}.
     *
     * @param html       the HTML content to render
     * @param outputPath the destination file path for the generated PDF
     * @throws FolioIOException if the file cannot be written (I/O error)
     * @throws FolioException if conversion fails for other reasons
     */
    public static void toPdf(String html, java.nio.file.Path outputPath) {
        toPdf(html, outputPath.toString());
    }

    /**
     * Converts an HTML string to a PDF and returns the raw bytes using
     * {@link PageSize#LETTER} dimensions.
     *
     * @param html the HTML content to render
     * @return the generated PDF as a byte array
     * @throws FolioException if the conversion fails
     */
    public static byte[] toBuffer(String html) {
        return toBuffer(html, PageSize.LETTER.width(), PageSize.LETTER.height());
    }

    /**
     * Converts an HTML string to a PDF with custom page dimensions and returns
     * the raw bytes.
     *
     * @param html       the HTML content to render
     * @param pageWidth  page width in points
     * @param pageHeight page height in points
     * @return the generated PDF as a byte array
     * @throws FolioException if the conversion fails
     */
    public static byte[] toBuffer(String html, double pageWidth, double pageHeight) {
        long buf = FolioNative.htmlToBuffer(html, pageWidth, pageHeight);
        if (buf == 0) throw new FolioException("Failed to convert HTML to buffer: " + FolioNative.lastError());
        try {
            int len = FolioNative.bufferLen(buf);
            MemorySegment data = FolioNative.bufferData(buf);
            return data.reinterpret(len).toArray(java.lang.foreign.ValueLayout.JAVA_BYTE);
        } finally {
            FolioNative.bufferFree(buf);
        }
    }

    /**
     * Converts HTML to PDF and returns the result as a byte array.
     * Useful for HTTP responses and in-memory processing.
     * Uses {@link PageSize#LETTER} dimensions.
     *
     * @param html the HTML content to render
     * @return the generated PDF as a byte array
     * @throws FolioException if the conversion fails
     */
    public static byte[] toBytes(String html) {
        return toBytes(html, PageSize.LETTER.width(), PageSize.LETTER.height());
    }

    /**
     * Converts HTML to PDF and returns the result as a byte array with custom
     * page dimensions.
     *
     * @param html       the HTML content to render
     * @param pageWidth  page width in points
     * @param pageHeight page height in points
     * @return the generated PDF as a byte array
     * @throws FolioException if the conversion fails
     */
    public static byte[] toBytes(String html, double pageWidth, double pageHeight) {
        long buf = FolioNative.htmlToBuffer(html, pageWidth, pageHeight);
        if (buf == 0) throw new FolioException("HTML to buffer failed: " + FolioNative.lastError());
        try {
            return FolioNative.bufferToByteArray(buf);
        } finally {
            FolioNative.bufferFree(buf);
        }
    }

    /**
     * Converts an HTML string to a {@link Document} using {@link PageSize#LETTER} dimensions.
     * The caller is responsible for closing the returned document.
     *
     * @param html the HTML content to render
     * @return a new {@link Document} representing the converted HTML
     * @throws FolioException if the conversion fails
     */
    public static Document toDocument(String html) {
        return toDocument(html, PageSize.LETTER.width(), PageSize.LETTER.height());
    }

    /**
     * Converts an HTML string to a {@link Document} with custom page dimensions.
     * The caller is responsible for closing the returned document.
     *
     * @param html       the HTML content to render
     * @param pageWidth  page width in points
     * @param pageHeight page height in points
     * @return a new {@link Document} representing the converted HTML
     * @throws FolioException if the conversion fails
     */
    public static Document toDocument(String html, double pageWidth, double pageHeight) {
        long h = FolioNative.htmlConvert(html, pageWidth, pageHeight);
        if (h == 0) throw new FolioException("Failed to convert HTML to document: " + FolioNative.lastError());
        return Document.fromHandle(h);
    }
}
