package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * A page-break element that forces subsequent content onto a new page.
 *
 * <pre>{@code
 * doc.add(Paragraph.of("End of page one.", Font.helvetica(), 12));
 * doc.add(AreaBreak.of());
 * doc.add(Paragraph.of("Start of page two.", Font.helvetica(), 12));
 * }</pre>
 */
public final class AreaBreak implements Element {

    private final long handle;

    private AreaBreak(long handle) {
        this.handle = handle;
    }

    /**
     * Creates a new area break (page break) element.
     *
     * @return a new {@code AreaBreak} instance
     * @throws FolioException if the native element cannot be created
     */
    public static AreaBreak of() {
        long h = FolioNative.areaBreakNew();
        if (h == 0) throw new FolioException("Failed to create area break: " + FolioNative.lastError());
        return new AreaBreak(h);
    }

    /**
     * Returns the native handle for this area break.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle;
    }
}
