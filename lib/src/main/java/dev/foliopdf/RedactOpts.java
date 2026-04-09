package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Configures how {@link PdfRedactor} blackouts are rendered — fill color,
 * optional overlay text, and whether to strip document metadata.
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to free the
 * underlying native handle.
 *
 * <pre>{@code
 * try (var opts = new RedactOpts()
 *         .fillColor(0, 0, 0)
 *         .overlay("REDACTED", 8, 1, 1, 1)
 *         .stripMetadata(true)) {
 *     byte[] redacted = PdfRedactor.text(reader, List.of("Social"), opts);
 * }
 * }</pre>
 */
public final class RedactOpts implements AutoCloseable {

    private final HandleRef handle;

    /** Creates a new redaction options object with default settings. */
    public RedactOpts() {
        long h = FolioNative.redactOptsNew();
        if (h == 0) throw new FolioException("Failed to create redact opts: " + FolioNative.lastError());
        this.handle = new HandleRef(h, FolioNative::redactOptsFree);
    }

    /**
     * Sets the fill color for the redaction rectangles.
     *
     * @param r red channel in {@code [0.0, 1.0]}
     * @param g green channel in {@code [0.0, 1.0]}
     * @param b blue channel in {@code [0.0, 1.0]}
     * @return this options object, for chaining
     */
    public RedactOpts fillColor(double r, double g, double b) {
        FolioNative.redactOptsSetFillColor(handle.get(), r, g, b);
        return this;
    }

    /**
     * Sets overlay text drawn on top of each redaction rectangle.
     *
     * @param text     the overlay label (e.g., {@code "REDACTED"})
     * @param fontSize font size in points
     * @param r        text red channel
     * @param g        text green channel
     * @param b        text blue channel
     * @return this options object, for chaining
     */
    public RedactOpts overlay(String text, double fontSize, double r, double g, double b) {
        FolioNative.redactOptsSetOverlay(handle.get(), text, fontSize, r, g, b);
        return this;
    }

    /**
     * Controls whether document metadata (author, title, etc.) is stripped.
     *
     * @param strip {@code true} to remove metadata from the redacted output
     * @return this options object, for chaining
     */
    public RedactOpts stripMetadata(boolean strip) {
        FolioNative.redactOptsSetStripMetadata(handle.get(), strip);
        return this;
    }

    /** Returns the underlying native handle. */
    public long handle() {
        return handle.get();
    }

    @Override
    public void close() {
        handle.close();
    }
}
