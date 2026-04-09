package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents a PDF font. Standard built-in fonts are obtained via static factory methods
 * such as {@link #helvetica()} or {@link #timesRoman()}. Custom TrueType fonts can be
 * loaded from a file path with {@link #loadTTF(String)} or from raw bytes with
 * {@link #parseTTF(byte[])}.
 *
 * <pre>{@code
 * Font body = Font.helvetica();
 * Font custom = Font.loadTTF("/fonts/MyFont.ttf");
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources for custom (TTF) fonts
 * to free the underlying native handle. Standard fonts do not own their handle and
 * {@link #close()} is a no-op for them.
 */
public final class Font implements AutoCloseable {

    private final HandleRef handle;
    private final boolean ownsHandle;

    private Font(long handle, boolean ownsHandle) {
        if (ownsHandle) {
            this.handle = new HandleRef(handle, FolioNative::fontFree);
        } else {
            this.handle = new HandleRef(handle, h -> {});
        }
        this.ownsHandle = ownsHandle;
    }

    /** @return the standard PDF Helvetica font. */
    public static Font helvetica() {
        long h = FolioNative.fontHelvetica();
        if (h == 0) throw new FolioException("Failed to create Helvetica font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Helvetica Bold font. */
    public static Font helveticaBold() {
        long h = FolioNative.fontHelveticaBold();
        if (h == 0) throw new FolioException("Failed to create Helvetica Bold font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Times Roman font. */
    public static Font timesRoman() {
        long h = FolioNative.fontTimesRoman();
        if (h == 0) throw new FolioException("Failed to create Times Roman font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Times Bold font. */
    public static Font timesBold() {
        long h = FolioNative.fontTimesBold();
        if (h == 0) throw new FolioException("Failed to create Times Bold font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Courier font. */
    public static Font courier() {
        long h = FolioNative.fontCourier();
        if (h == 0) throw new FolioException("Failed to create Courier font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Courier Bold font. */
    public static Font courierBold() {
        long h = FolioNative.fontCourierBold();
        if (h == 0) throw new FolioException("Failed to create Courier Bold font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Courier Oblique (italic) font. */
    public static Font courierOblique() {
        long h = FolioNative.fontCourierOblique();
        if (h == 0) throw new FolioException("Failed to create Courier Oblique font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Courier Bold Oblique font. */
    public static Font courierBoldOblique() {
        long h = FolioNative.fontCourierBoldOblique();
        if (h == 0) throw new FolioException("Failed to create Courier Bold Oblique font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Helvetica Oblique (italic) font. */
    public static Font helveticaOblique() {
        long h = FolioNative.fontHelveticaOblique();
        if (h == 0) throw new FolioException("Failed to create Helvetica Oblique font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Helvetica Bold Oblique font. */
    public static Font helveticaBoldOblique() {
        long h = FolioNative.fontHelveticaBoldOblique();
        if (h == 0) throw new FolioException("Failed to create Helvetica Bold Oblique font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Times Italic font. */
    public static Font timesItalic() {
        long h = FolioNative.fontTimesItalic();
        if (h == 0) throw new FolioException("Failed to create Times Italic font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Times Bold Italic font. */
    public static Font timesBoldItalic() {
        long h = FolioNative.fontTimesBoldItalic();
        if (h == 0) throw new FolioException("Failed to create Times Bold Italic font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF Symbol font. */
    public static Font symbol() {
        long h = FolioNative.fontSymbol();
        if (h == 0) throw new FolioException("Failed to create Symbol font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /** @return the standard PDF ZapfDingbats font. */
    public static Font zapfDingbats() {
        long h = FolioNative.fontZapfDingbats();
        if (h == 0) throw new FolioException("Failed to create Zapf Dingbats font: " + FolioNative.lastError());
        return new Font(h, false);
    }

    /**
     * Returns one of the 14 standard PDF fonts by name (e.g., {@code "Helvetica"}).
     *
     * @param name the standard font name
     * @return the corresponding standard {@link Font}
     * @throws FolioException if the name is not recognised
     */
    public static Font standard(String name) {
        long h = FolioNative.fontStandard(name);
        if (h == 0) throw new FolioException("Failed to create standard font: " + name + ": " + FolioNative.lastError());
        return new Font(h, false);
    }

    /**
     * Loads a TrueType font from a file path. The returned font owns its handle
     * and should be closed when no longer needed.
     *
     * @param path absolute path to the {@code .ttf} file
     * @return a new {@link Font} backed by the loaded TrueType data
     * @throws FolioException if the file cannot be read or parsed
     */
    public static Font loadTTF(String path) {
        long h = FolioNative.fontLoadTTF(path);
        if (h == 0) throw new FolioIOException("Failed to load TTF font: " + path + ": " + FolioNative.lastError());
        return new Font(h, true);
    }

    /**
     * Loads a TrueType font from a {@link java.nio.file.Path}. The returned font
     * owns its handle and should be closed when no longer needed.
     *
     * @param path path to the {@code .ttf} file
     * @return a new {@link Font} backed by the loaded TrueType data
     * @throws FolioException if the file cannot be read or parsed
     */
    public static Font loadTTF(java.nio.file.Path path) {
        return loadTTF(path.toString());
    }

    /**
     * Loads a TrueType font from an {@link java.io.InputStream} by consuming all bytes.
     * The returned font owns its handle and should be closed when no longer needed.
     *
     * @param in the input stream to read from (fully consumed)
     * @return a new {@link Font} backed by the parsed TrueType data
     * @throws java.io.IOException if an I/O error occurs while reading the stream
     * @throws FolioException if the bytes cannot be parsed as a valid font
     */
    public static Font loadTTF(java.io.InputStream in) throws java.io.IOException {
        return parseTTF(in.readAllBytes());
    }

    /**
     * Parses a TrueType font from raw bytes. The returned font owns its handle
     * and should be closed when no longer needed.
     *
     * @param data the raw {@code .ttf} file bytes
     * @return a new {@link Font} backed by the parsed TrueType data
     * @throws FolioException if the bytes cannot be parsed as a valid font
     */
    public static Font parseTTF(byte[] data) {
        long h = FolioNative.fontParseTTF(data);
        if (h == 0) throw new FolioException("Failed to parse TTF font from bytes: " + FolioNative.lastError());
        return new Font(h, true);
    }

    /**
     * Returns the native handle for this font.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }

    /**
     * Frees the underlying native handle for custom (TTF) fonts. No-op for
     * standard built-in fonts. Called automatically by try-with-resources.
     */
    @Override
    public void close() {
        if (ownsHandle) {
            handle.close();
        }
    }
}
