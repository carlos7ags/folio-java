package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * A horizontal rule element that draws a full-width dividing line across the page.
 *
 * <pre>{@code
 * doc.add(Paragraph.of("Section One", Font.helvetica(), 14));
 * doc.add(LineSeparator.of());
 * doc.add(Paragraph.of("Section Two", Font.helvetica(), 14));
 * }</pre>
 */
public final class LineSeparator implements Element {

    private final long handle;

    private LineSeparator(long handle) {
        this.handle = handle;
    }

    /**
     * Creates a new horizontal line separator element.
     *
     * @return a new {@code LineSeparator} instance
     * @throws FolioException if the native element cannot be created
     */
    public static LineSeparator of() {
        long h = FolioNative.lineSeparatorNew();
        if (h == 0) throw new FolioException("Failed to create line separator: " + FolioNative.lastError());
        return new LineSeparator(h);
    }

    /**
     * Returns the native handle for this line separator.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle;
    }
}
