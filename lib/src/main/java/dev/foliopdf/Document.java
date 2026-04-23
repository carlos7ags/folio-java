package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Represents a PDF document. Create instances using the {@link #builder()} method.
 *
 * <pre>{@code
 * try (var doc = Document.builder()
 *         .pageSize(PageSize.A4)
 *         .margins(36, 36, 36, 36)
 *         .title("Q3 Report")
 *         .build()) {
 *
 *     doc.add(Heading.of("Q3 Report", HeadingLevel.H1));
 *     doc.add(Paragraph.of("Revenue grew 23% year over year."));
 *     doc.save("report.pdf");
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to ensure
 * the underlying native handle is freed.
 */
public final class Document implements AutoCloseable {

    private final HandleRef handle;
    private Arena callbackArena;

    private Document(long handle) {
        this.handle = new HandleRef(handle, FolioNative::documentFree);
    }

    static Document fromHandle(long handle) {
        return new Document(handle);
    }

    /**
     * Creates a document with default settings (Letter size, 36pt margins),
     * passes it to the consumer, saves to the given path, and closes it.
     *
     * <pre>{@code
     * Document.create("report.pdf", doc -> {
     *     doc.add(Heading.of("Title", HeadingLevel.H1));
     *     doc.add(Paragraph.of("Body text"));
     * });
     * }</pre>
     *
     * @param path    the output file path
     * @param builder a consumer that populates the document
     */
    public static void create(String path, java.util.function.Consumer<Document> builder) {
        try (var doc = Document.builder().letter().margins(36).build()) {
            builder.accept(doc);
            doc.save(path);
        }
    }

    /**
     * Creates a document with default settings (Letter size, 36pt margins),
     * passes it to the consumer, saves to the given path, and closes it.
     *
     * @param path    the output file path
     * @param builder a consumer that populates the document
     */
    public static void create(java.nio.file.Path path, java.util.function.Consumer<Document> builder) {
        create(path.toString(), builder);
    }

    /**
     * Creates a document with default settings (Letter size, 36pt margins),
     * passes it to the consumer, and returns the PDF bytes.
     *
     * @param builder a consumer that populates the document
     * @return the rendered PDF as a byte array
     */
    public static byte[] create(java.util.function.Consumer<Document> builder) {
        try (var doc = Document.builder().letter().margins(36).build()) {
            builder.accept(doc);
            return doc.toBytes();
        }
    }

    /**
     * Creates a default Letter-size document with no metadata or custom margins.
     * This is a convenience shorthand for {@code Document.builder().build()}.
     *
     * @return a new {@link Document} with Letter page size
     * @throws FolioException if the native document creation call fails
     */
    public static Document of() {
        return builder().build();
    }

    /**
     * Creates a document with the given page size and no metadata or custom margins.
     *
     * @param pageSize the desired page size
     * @return a new {@link Document}
     * @throws FolioException if the native document creation call fails
     */
    public static Document of(PageSize pageSize) {
        return builder().pageSize(pageSize).build();
    }

    /**
     * Returns a new builder for constructing a {@link Document}.
     *
     * @return a fresh {@link Builder} instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Appends a {@link Paragraph} to this document.
     *
     * @param paragraph the paragraph to add
     * @return this document, for chaining
     */
    public Document add(Paragraph paragraph) {
        FolioNative.documentAdd(handle.get(), paragraph.handle());
        return this;
    }

    /**
     * Appends a {@link Heading} to this document.
     *
     * @param heading the heading to add
     * @return this document, for chaining
     */
    public Document add(Heading heading) {
        FolioNative.documentAdd(handle.get(), heading.handle());
        return this;
    }

    /**
     * Appends a {@link Table} to this document.
     *
     * @param table the table to add
     * @return this document, for chaining
     */
    public Document add(Table table) {
        FolioNative.documentAdd(handle.get(), table.handle());
        return this;
    }

    /**
     * Appends a {@link Div} container to this document.
     *
     * @param div the div to add
     * @return this document, for chaining
     */
    public Document add(Div div) {
        FolioNative.documentAdd(handle.get(), div.handle());
        return this;
    }

    /**
     * Appends a {@link ListElement} to this document.
     *
     * @param list the list to add
     * @return this document, for chaining
     */
    public Document add(ListElement list) {
        FolioNative.documentAdd(handle.get(), list.handle());
        return this;
    }

    /**
     * Appends an {@link Image} to this document.
     *
     * @param image the image to add
     * @return this document, for chaining
     */
    public Document add(Image image) {
        FolioNative.documentAdd(handle.get(), image.handle());
        return this;
    }

    /**
     * Appends a {@link LineSeparator} to this document.
     *
     * @param separator the line separator to add
     * @return this document, for chaining
     */
    public Document add(LineSeparator separator) {
        FolioNative.documentAdd(handle.get(), separator.handle());
        return this;
    }

    /**
     * Appends an {@link AreaBreak} (page break) to this document.
     *
     * @param areaBreak the area break to add
     * @return this document, for chaining
     */
    public Document add(AreaBreak areaBreak) {
        FolioNative.documentAdd(handle.get(), areaBreak.handle());
        return this;
    }

    /**
     * Appends a {@link Link} element to this document.
     *
     * @param link the link to add
     * @return this document, for chaining
     */
    public Document add(Link link) {
        FolioNative.documentAdd(handle.get(), link.handle());
        return this;
    }

    /**
     * Appends a {@link Barcode} element to this document.
     *
     * @param barcode the barcode to add
     * @return this document, for chaining
     */
    public Document add(Barcode barcode) {
        FolioNative.documentAdd(handle.get(), barcode.handle());
        return this;
    }

    /**
     * Appends an {@link SvgElement} to this document.
     *
     * @param svg the SVG element to add
     * @return this document, for chaining
     */
    public Document add(SvgElement svg) {
        FolioNative.documentAdd(handle.get(), svg.handle());
        return this;
    }

    /**
     * Appends a {@link Flex} layout element to this document.
     *
     * @param flex the flex element to add
     * @return this document, for chaining
     */
    public Document add(Flex flex) {
        FolioNative.documentAdd(handle.get(), flex.handle());
        return this;
    }

    /**
     * Appends a {@link Grid} layout element to this document.
     *
     * @param grid the grid element to add
     * @return this document, for chaining
     */
    public Document add(Grid grid) {
        FolioNative.documentAdd(handle.get(), grid.handle());
        return this;
    }

    /**
     * Appends a {@link Columns} layout element to this document.
     *
     * @param columns the columns element to add
     * @return this document, for chaining
     */
    public Document add(Columns columns) {
        FolioNative.documentAdd(handle.get(), columns.handle());
        return this;
    }

    /**
     * Appends a {@link FloatElement} to this document.
     *
     * @param floatElement the float element to add
     * @return this document, for chaining
     */
    public Document add(FloatElement floatElement) {
        FolioNative.documentAdd(handle.get(), floatElement.handle());
        return this;
    }

    /**
     * Appends a {@link TabbedLine} to this document.
     *
     * @param tabbedLine the tabbed line to add
     * @return this document, for chaining
     */
    public Document add(TabbedLine tabbedLine) {
        FolioNative.documentAdd(handle.get(), tabbedLine.handle());
        return this;
    }

    /**
     * Adds any {@link Element} to this document. This is a generic alternative
     * to the type-specific {@code add()} overloads.
     *
     * @param element the element to add
     * @return this document, for chaining
     */
    public Document add(Element element) {
        FolioNative.documentAdd(handle.get(), element.handle());
        return this;
    }

    /**
     * Adds a new blank page and returns it for absolute-position content placement.
     *
     * @return the newly created {@link Page}
     * @throws FolioException if the native call fails
     */
    public Page addPage() {
        long h = FolioNative.documentAddPage(handle.get());
        if (h == 0) throw new FolioException("Failed to add page: " + FolioNative.lastError());
        return new Page(h);
    }

    /**
     * Returns the number of pages currently in the document.
     *
     * @return the page count
     */
    public int pageCount() {
        return FolioNative.documentPageCount(handle.get());
    }

    /**
     * Enables or disables PDF tagged (accessibility) output.
     *
     * @param enabled {@code true} to produce a tagged PDF
     * @return this document, for chaining
     */
    public Document tagged(boolean enabled) {
        FolioNative.documentSetTagged(handle.get(), enabled);
        return this;
    }

    /**
     * Sets the PDF/A conformance level for archival output.
     *
     * @param level the {@link PdfALevel} to target
     * @return this document, for chaining
     */
    public Document pdfA(PdfALevel level) {
        FolioNative.documentSetPdfA(handle.get(), level.value());
        return this;
    }

    /**
     * Applies password-based encryption to the output PDF.
     *
     * @param userPassword  password required to open the document
     * @param ownerPassword password granting full owner permissions
     * @param algorithm     the {@link EncryptionAlgorithm} to use
     * @return this document, for chaining
     */
    public Document encryption(String userPassword, String ownerPassword, EncryptionAlgorithm algorithm) {
        FolioNative.documentSetEncryption(handle.get(), userPassword, ownerPassword, algorithm.value());
        return this;
    }

    /**
     * Applies password-based encryption with granular permission flags.
     *
     * @param userPassword  password required to open the document
     * @param ownerPassword password granting full owner permissions
     * @param algorithm     the {@link EncryptionAlgorithm} to use
     * @param permissions   bitwise OR of {@link PdfPermission} flags
     * @return this document, for chaining
     */
    public Document encryption(String userPassword, String ownerPassword, EncryptionAlgorithm algorithm, int permissions) {
        FolioNative.documentSetEncryptionWithPermissions(handle.get(), userPassword, ownerPassword, algorithm.value(), permissions);
        return this;
    }

    /**
     * Enables or disables automatic bookmark generation from headings.
     *
     * @param enabled {@code true} to generate bookmarks automatically
     * @return this document, for chaining
     */
    public Document autoBookmarks(boolean enabled) {
        FolioNative.documentSetAutoBookmarks(handle.get(), enabled);
        return this;
    }

    /**
     * Attaches an interactive {@link Form} to this document.
     *
     * @param form the form to attach
     * @return this document, for chaining
     */
    public Document form(Form form) {
        FolioNative.documentSetForm(handle.get(), form.handle());
        return this;
    }

    /**
     * Sets a repeating header rendered on every page via the given decorator callback.
     *
     * @param decorator callback that draws header content on each page
     * @return this document, for chaining
     */
    public Document header(PageDecorator decorator) {
        ensureCallbackArena();
        MemorySegment stub = FolioNative.createDecoratorStub(decorator, callbackArena);
        FolioNative.documentSetHeader(handle.get(), stub);
        return this;
    }

    /**
     * Sets a repeating footer rendered on every page via the given decorator callback.
     *
     * @param decorator callback that draws footer content on each page
     * @return this document, for chaining
     */
    public Document footer(PageDecorator decorator) {
        ensureCallbackArena();
        MemorySegment stub = FolioNative.createDecoratorStub(decorator, callbackArena);
        FolioNative.documentSetFooter(handle.get(), stub);
        return this;
    }

    private void ensureCallbackArena() {
        if (callbackArena == null) {
            callbackArena = Arena.ofShared();
        }
    }

    /**
     * Adds a simple text watermark to every page using default styling.
     *
     * @param text the watermark text
     * @return this document, for chaining
     */
    public Document watermark(String text) {
        FolioNative.documentSetWatermark(handle.get(), text);
        return this;
    }

    /**
     * Adds a text watermark to every page with custom font size, color, angle, and opacity.
     *
     * @param text      the watermark text
     * @param fontSize  font size in points
     * @param color     text color
     * @param angle     rotation angle in degrees
     * @param opacity   opacity in the range {@code [0.0, 1.0]}
     * @return this document, for chaining
     */
    public Document watermark(String text, double fontSize, Color color, double angle, double opacity) {
        FolioNative.documentSetWatermarkConfig(handle.get(), text, fontSize, color.r(), color.g(), color.b(), angle, opacity);
        return this;
    }

    /**
     * Adds a top-level PDF outline (bookmark) entry pointing to a page.
     *
     * @param title     the bookmark label
     * @param pageIndex zero-based target page index
     * @return the native outline handle, usable with {@link #outlineAddChild}
     * @throws FolioException if the native call fails
     */
    public long addOutline(String title, int pageIndex) {
        long h = FolioNative.documentAddOutline(handle.get(), title, pageIndex);
        if (h == 0) throw new FolioException("Failed to add outline: " + FolioNative.lastError());
        return h;
    }

    /**
     * Adds a top-level PDF outline entry with an explicit XYZ destination.
     *
     * @param title     the bookmark label
     * @param pageIndex zero-based target page index
     * @param left      left coordinate of the destination view
     * @param top       top coordinate of the destination view
     * @param zoom      zoom factor at the destination
     * @return the native outline handle, usable with {@link #outlineAddChild}
     * @throws FolioException if the native call fails
     */
    public long addOutline(String title, int pageIndex, double left, double top, double zoom) {
        long h = FolioNative.documentAddOutlineXyz(handle.get(), title, pageIndex, left, top, zoom);
        if (h == 0) throw new FolioException("Failed to add outline: " + FolioNative.lastError());
        return h;
    }

    /**
     * Adds a child bookmark under an existing outline entry.
     *
     * @param outline   the parent outline handle
     * @param title     the bookmark label
     * @param pageIndex zero-based target page index
     * @return the native child outline handle
     * @throws FolioException if the native call fails
     */
    public static long outlineAddChild(long outline, String title, int pageIndex) {
        long h = FolioNative.outlineAddChild(outline, title, pageIndex);
        if (h == 0) throw new FolioException("Failed to add outline child: " + FolioNative.lastError());
        return h;
    }

    /**
     * Adds a child bookmark under an existing outline entry with an explicit XYZ destination.
     *
     * @param outline   the parent outline handle
     * @param title     the bookmark label
     * @param pageIndex zero-based target page index
     * @param left      left coordinate of the destination view
     * @param top       top coordinate of the destination view
     * @param zoom      zoom factor at the destination
     * @return the native child outline handle
     * @throws FolioException if the native call fails
     */
    public static long outlineAddChild(long outline, String title, int pageIndex, double left, double top, double zoom) {
        long h = FolioNative.outlineAddChildXyz(outline, title, pageIndex, left, top, zoom);
        if (h == 0) throw new FolioException("Failed to add outline child: " + FolioNative.lastError());
        return h;
    }

    /**
     * Adds a named destination that can be targeted by internal links.
     *
     * @param name      unique destination name
     * @param pageIndex zero-based target page index
     * @param fitType   PDF fit type string (e.g., {@code "XYZ"})
     * @param top       top coordinate
     * @param left      left coordinate
     * @param zoom      zoom factor
     * @return this document, for chaining
     */
    public Document addNamedDest(String name, int pageIndex, String fitType, double top, double left, double zoom) {
        FolioNative.documentAddNamedDest(handle.get(), name, pageIndex, fitType, top, left, zoom);
        return this;
    }

    /**
     * Configures PDF viewer preferences for how the document is displayed when opened.
     *
     * @param pageLayout     PDF page layout name (e.g., {@code "SinglePage"})
     * @param pageMode       PDF page mode name (e.g., {@code "UseOutlines"})
     * @param hideToolbar    whether to hide the viewer toolbar
     * @param hideMenubar    whether to hide the viewer menu bar
     * @param hideWindowUI   whether to hide the viewer window UI
     * @param fitWindow      whether to fit the window to the first page
     * @param centerWindow   whether to center the window on screen
     * @param displayDocTitle whether to display the document title in the title bar
     * @return this document, for chaining
     */
    public Document viewerPreferences(String pageLayout, String pageMode,
            boolean hideToolbar, boolean hideMenubar, boolean hideWindowUI,
            boolean fitWindow, boolean centerWindow, boolean displayDocTitle) {
        FolioNative.documentSetViewerPreferences(handle.get(), pageLayout, pageMode,
            hideToolbar, hideMenubar, hideWindowUI, fitWindow, centerWindow, displayDocTitle);
        return this;
    }

    /**
     * Adds a page label range starting at the given page index.
     *
     * @param pageIndex zero-based page index where the label range starts
     * @param style     numbering style (e.g., {@code "D"} for decimal, {@code "r"} for lowercase roman)
     * @param prefix    optional text prefix for each label
     * @param start     the numeric start value for this range
     * @return this document, for chaining
     */
    public Document addPageLabel(int pageIndex, String style, String prefix, int start) {
        FolioNative.documentAddPageLabel(handle.get(), pageIndex, style, prefix, start);
        return this;
    }

    /**
     * Removes the page at the given zero-based index from the document.
     *
     * @param index zero-based page index to remove
     * @return this document, for chaining
     */
    public Document removePage(int index) {
        FolioNative.documentRemovePage(handle.get(), index);
        return this;
    }

    /**
     * Adds a native element handle at an absolute position on the current page.
     *
     * @param element the native element handle
     * @param x       x coordinate in points from the left edge
     * @param y       y coordinate in points from the top edge
     * @param width   available width in points
     * @return this document, for chaining
     */
    public Document addAbsolute(long element, double x, double y, double width) {
        FolioNative.documentAddAbsolute(handle.get(), element, x, y, width);
        return this;
    }

    /**
     * Attaches a file as an embedded file stream in the PDF.
     *
     * @param data           the raw file bytes
     * @param fileName       the name to assign to the attachment
     * @param mimeType       MIME type of the attachment (e.g., {@code "text/plain"})
     * @param description    human-readable description of the attachment
     * @param afRelationship PDF AF relationship value (e.g., {@code "Data"})
     * @return this document, for chaining
     */
    public Document attachFile(byte[] data, String fileName, String mimeType, String description, String afRelationship) {
        FolioNative.documentAttachFile(handle.get(), data, fileName, mimeType, description, afRelationship);
        return this;
    }

    /**
     * Appends an HTML fragment to the document using default rendering options.
     *
     * @param html the HTML content to render
     * @return this document, for chaining
     */
    public Document addHtml(String html) {
        FolioNative.documentAddHtml(handle.get(), html);
        return this;
    }

    /**
     * Appends an HTML fragment with explicit rendering options.
     *
     * @param html              the HTML content to render
     * @param defaultFontSize   base font size in points
     * @param pageWidth         page width in points used for layout
     * @param pageHeight        page height in points used for layout
     * @param basePath          base path for resolving relative resource URLs
     * @param fallbackFontPath  path to a fallback font file
     * @return this document, for chaining
     */
    public Document addHtml(String html, double defaultFontSize, double pageWidth, double pageHeight, String basePath, String fallbackFontPath) {
        FolioNative.documentAddHtmlWithOptions(handle.get(), html, defaultFontSize, pageWidth, pageHeight, basePath, fallbackFontPath);
        return this;
    }

    /**
     * Sets custom margins for the first page of the document.
     *
     * @param top    top margin in points
     * @param right  right margin in points
     * @param bottom bottom margin in points
     * @param left   left margin in points
     * @return this document, for chaining
     */
    public Document firstMargins(double top, double right, double bottom, double left) {
        FolioNative.documentSetFirstMargins(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Sets custom margins for left (even-numbered) pages in a duplex layout.
     *
     * @param top    top margin in points
     * @param right  right margin in points
     * @param bottom bottom margin in points
     * @param left   left margin in points
     * @return this document, for chaining
     */
    public Document leftMargins(double top, double right, double bottom, double left) {
        FolioNative.documentSetLeftMargins(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Sets custom margins for right (odd-numbered) pages in a duplex layout.
     *
     * @param top    top margin in points
     * @param right  right margin in points
     * @param bottom bottom margin in points
     * @param left   left margin in points
     * @return this document, for chaining
     */
    public Document rightMargins(double top, double right, double bottom, double left) {
        FolioNative.documentSetRightMargins(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Saves the document to a file at the given path.
     *
     * @param path the output file path
     * @throws FolioIOException if the save fails due to a file I/O error
     * @throws FolioException if the native save call fails for other reasons
     */
    public void save(String path) {
        FolioNative.documentSave(handle.get(), path);
    }

    /**
     * Saves the document to a file at the given {@link java.nio.file.Path}.
     *
     * @param path the output file path
     * @throws FolioIOException if the save fails due to a file I/O error
     * @throws FolioException if the native save call fails for other reasons
     */
    public void save(java.nio.file.Path path) {
        save(path.toString());
    }

    /**
     * Saves the document to a file with explicit writer options. Pass
     * {@code null} to use the engine's default writer settings.
     *
     * <p>See {@link WriteOptions} for the per-feature toggles. The PDF
     * file structure produced by the writer is described in
     * ISO 32000-1 §7.5 (File Structure).
     *
     * @param path    the output file path
     * @param options the writer options, or {@code null} for defaults
     * @throws FolioIOException if the save fails due to a file I/O error
     * @throws FolioException if the native save call fails for other reasons
     * @since 0.7.1
     */
    public void saveWithOptions(String path, WriteOptions options) {
        long optsHandle = (options == null) ? 0L : options.handle();
        FolioNative.documentSaveWithOptions(handle.get(), path, optsHandle);
    }

    /**
     * Saves the document to a file with explicit writer options. Pass
     * {@code null} to use the engine's default writer settings.
     *
     * @param path    the output file path
     * @param options the writer options, or {@code null} for defaults
     * @throws FolioIOException if the save fails due to a file I/O error
     * @throws FolioException if the native save call fails for other reasons
     * @since 0.7.1
     */
    public void saveWithOptions(java.nio.file.Path path, WriteOptions options) {
        saveWithOptions(path.toString(), options);
    }

    /**
     * Renders the document to an in-memory byte array using explicit writer
     * options. Pass {@code null} to use the engine's default writer settings.
     *
     * @param options the writer options, or {@code null} for defaults
     * @return the PDF bytes
     * @throws FolioException if the native write call fails
     * @since 0.7.1
     */
    public byte[] toBytesWithOptions(WriteOptions options) {
        long optsHandle = (options == null) ? 0L : options.handle();
        long buf = FolioNative.documentWriteToBufferWithOptions(handle.get(), optsHandle);
        if (buf == 0) throw new FolioException("Failed to write document to buffer: " + FolioNative.lastError());
        return FolioNative.bufferToByteArray(buf);
    }

    /**
     * Toggles emission of {@code /ActualText} entries in the marked-content
     * sequences of tagged PDFs (ISO 32000-1 §14.9.4).
     *
     * <p>{@code /ActualText} provides assistive technologies with the
     * canonical Unicode text for a marked region; turn it off to reduce file
     * size when accessibility is not required (or when the visible glyphs
     * already match the logical text).
     *
     * @param enabled {@code true} to emit {@code /ActualText} entries
     * @return this document, for chaining
     * @since 0.7.1
     */
    public Document setActualText(boolean enabled) {
        FolioNative.documentSetActualText(handle.get(), enabled);
        return this;
    }

    /**
     * Writes the rendered PDF bytes to the given output stream.
     *
     * @param out the target output stream
     * @throws java.io.IOException if an I/O error occurs while writing
     * @throws FolioException if the native render call fails
     */
    public void writeTo(java.io.OutputStream out) throws java.io.IOException {
        byte[] data = toBytes();
        out.write(data);
    }

    /**
     * Renders the document to an in-memory byte array.
     *
     * @return the PDF bytes
     * @throws FolioException if the native write call fails
     */
    public byte[] writeToBuffer() {
        long buf = FolioNative.documentWriteToBuffer(handle.get());
        if (buf == 0) throw new FolioException("Failed to write document to buffer: " + FolioNative.lastError());
        try {
            int len = FolioNative.bufferLen(buf);
            MemorySegment data = FolioNative.bufferData(buf);
            return data.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
        } finally {
            FolioNative.bufferFree(buf);
        }
    }

    /**
     * Convenience wrapper around {@link #writeToBuffer()} that uses
     * {@code folio_document_to_bytes} internally.
     *
     * @return the PDF bytes
     * @throws FolioException if the native call fails
     */
    public byte[] toBytes() {
        long buf = FolioNative.documentToBytes(handle.get());
        if (buf == 0) throw new FolioException("Failed to serialize document to bytes: " + FolioNative.lastError());
        return FolioNative.bufferToByteArray(buf);
    }

    /**
     * Validates the document against its configured PDF/A conformance level.
     *
     * @throws FolioException if the document does not meet the configured level
     */
    public void validatePdfA() {
        FolioNative.documentValidatePdfA(handle.get());
    }

    /**
     * Sets a simple text header rendered on every page. The text may contain
     * {@code {page}} and {@code {pages}} placeholders.
     *
     * @param text  the header text
     * @param font  the font to use
     * @param size  font size in points
     * @param align horizontal alignment
     * @return this document, for chaining
     */
    public Document setHeaderText(String text, Font font, double size, Align align) {
        FolioNative.documentSetHeaderText(handle.get(), text, font.handle(), size, align.value());
        return this;
    }

    /**
     * Sets a simple text footer rendered on every page. The text may contain
     * {@code {page}} and {@code {pages}} placeholders.
     *
     * @param text  the footer text
     * @param font  the font to use
     * @param size  font size in points
     * @param align horizontal alignment
     * @return this document, for chaining
     */
    public Document setFooterText(String text, Font font, double size, Align align) {
        FolioNative.documentSetFooterText(handle.get(), text, font.handle(), size, align.value());
        return this;
    }

    /**
     * Frees the underlying native document handle. Called automatically by
     * try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
        if (callbackArena != null) {
            callbackArena.close();
            callbackArena = null;
        }
    }

    /**
     * Builder for creating a {@link Document}. Obtain an instance from
     * {@link Document#builder()}.
     */
    public static final class Builder {
        private PageSize pageSize;
        private String title;
        private String author;
        private double marginTop = -1;
        private double marginRight = -1;
        private double marginBottom = -1;
        private double marginLeft = -1;

        Builder() {}

        /**
         * Sets the page size for the document.
         *
         * @param pageSize the desired page size (e.g., {@link PageSize#A4})
         * @return this builder, for chaining
         */
        public Builder pageSize(PageSize pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        /** Shorthand for {@code pageSize(PageSize.A4)}. */
        public Builder a4() { return pageSize(PageSize.A4); }

        /** Shorthand for {@code pageSize(PageSize.LETTER)}. */
        public Builder letter() { return pageSize(PageSize.LETTER); }

        /**
         * Sets uniform margins on all four sides.
         *
         * @param all margin in points for all sides
         * @return this builder, for chaining
         */
        public Builder margins(double all) {
            return margins(all, all, all, all);
        }

        /**
         * Sets the document title metadata.
         *
         * @param title the document title
         * @return this builder, for chaining
         */
        public Builder title(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the document author metadata.
         *
         * @param author the document author
         * @return this builder, for chaining
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Sets the page margins in points.
         *
         * @param top    top margin
         * @param right  right margin
         * @param bottom bottom margin
         * @param left   left margin
         * @return this builder, for chaining
         */
        public Builder margins(double top, double right, double bottom, double left) {
            this.marginTop = top;
            this.marginRight = right;
            this.marginBottom = bottom;
            this.marginLeft = left;
            return this;
        }

        /**
         * Builds and returns the configured {@link Document}.
         *
         * @return a new {@link Document} instance
         * @throws FolioException if the native document creation call fails
         */
        public Document build() {
            long h;
            if (pageSize != null) {
                if (pageSize == PageSize.LETTER) {
                    h = FolioNative.documentNewLetter();
                } else if (pageSize == PageSize.A4) {
                    h = FolioNative.documentNewA4();
                } else {
                    h = FolioNative.documentNew(pageSize.width(), pageSize.height());
                }
            } else {
                h = FolioNative.documentNewLetter();
            }

            if (h == 0) throw new FolioException("Failed to create document: " + FolioNative.lastError());

            Document doc = new Document(h);

            if (title != null) {
                FolioNative.documentSetTitle(h, title);
            }
            if (author != null) {
                FolioNative.documentSetAuthor(h, author);
            }
            if (marginTop >= 0) {
                FolioNative.documentSetMargins(h, marginTop, marginRight, marginBottom, marginLeft);
            }

            return doc;
        }
    }
}
