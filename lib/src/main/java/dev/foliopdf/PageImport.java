package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A handle to a page extracted from an existing PDF that can be stamped onto
 * a new {@link Page} as a template or background.
 *
 * <pre>{@code
 * try (var reader = PdfReader.open("letterhead.pdf");
 *      var imp    = PageImport.from(reader, 0)) {
 *     Page p = doc.addPage();
 *     imp.applyTo(p);
 *     p.addText("Hello", Font.helvetica(), 12, 72, 700);
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 */
public final class PageImport implements AutoCloseable {

    private final HandleRef handle;

    private PageImport(long handle) {
        this.handle = new HandleRef(handle, FolioNative::pageImportFree);
    }

    /**
     * Extracts the given page from an open {@link PdfReader}.
     *
     * @param reader    source PDF reader
     * @param pageIndex zero-based page index
     * @return a new {@link PageImport} handle
     * @throws FolioException if extraction fails
     */
    public static PageImport from(PdfReader reader, int pageIndex) {
        long h = FolioNative.extractPageImport(reader.readerHandle(), pageIndex);
        if (h == 0) throw new FolioException("Failed to extract page import: " + FolioNative.lastError());
        return new PageImport(h);
    }

    /** Returns the source page width in points. */
    public double width() {
        return FolioNative.pageImportWidth(handle.get());
    }

    /** Returns the source page height in points. */
    public double height() {
        return FolioNative.pageImportHeight(handle.get());
    }

    /**
     * Stamps the imported page content onto the target page.
     *
     * @param page destination page
     */
    public void applyTo(Page page) {
        FolioNative.pageImportApply(page.handle(), handle.get());
    }

    /**
     * Frees the underlying native page import handle. Called automatically
     * by try-with-resources.
     */
    @Override
    public void close() {
        handle.close();
    }
}
