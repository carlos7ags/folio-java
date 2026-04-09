package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents an image element that can be added to a {@link Document} or {@link Div}.
 * Supports JPEG, PNG, and TIFF formats loaded from a file path or decoded from
 * raw bytes.
 *
 * <pre>{@code
 * try (var img = Image.loadPng("/assets/logo.png")) {
 *     doc.add(img.size(150, 75).align(Align.CENTER));
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources to release the
 * underlying native image and element handles.
 */
public final class Image implements AutoCloseable, Element {

    private final HandleRef imageHandle;
    private final HandleRef elementHandle;

    private Image(long imgHandle, long elemHandle) {
        this.imageHandle = new HandleRef(imgHandle, FolioNative::imageFree);
        this.elementHandle = new HandleRef(elemHandle, FolioNative::imageElementFree);
    }

    /**
     * Loads a JPEG image from the given file path.
     *
     * @param path absolute path to the {@code .jpg} / {@code .jpeg} file
     * @return a new {@link Image} ready to be added to a document
     * @throws FolioException if the file cannot be read or is not a valid JPEG
     */
    public static Image loadJpeg(String path) {
        long img = FolioNative.imageLoadJpeg(path);
        if (img == 0) throw new FolioIOException("Failed to load JPEG: " + path + ": " + FolioNative.lastError());
        long elem = FolioNative.imageElementNew(img);
        if (elem == 0) throw new FolioException("Failed to create image element: " + FolioNative.lastError());
        return new Image(img, elem);
    }

    /**
     * Loads a PNG image from the given file path.
     *
     * @param path absolute path to the {@code .png} file
     * @return a new {@link Image} ready to be added to a document
     * @throws FolioException if the file cannot be read or is not a valid PNG
     */
    public static Image loadPng(String path) {
        long img = FolioNative.imageLoadPng(path);
        if (img == 0) throw new FolioIOException("Failed to load PNG: " + path + ": " + FolioNative.lastError());
        long elem = FolioNative.imageElementNew(img);
        if (elem == 0) throw new FolioException("Failed to create image element: " + FolioNative.lastError());
        return new Image(img, elem);
    }

    /**
     * Parses a JPEG image from raw bytes.
     *
     * @param data the raw JPEG bytes
     * @return a new {@link Image} ready to be added to a document
     * @throws FolioException if the bytes are not valid JPEG data
     */
    public static Image parseJpeg(byte[] data) {
        long img = FolioNative.imageParseJpeg(data);
        if (img == 0) throw new FolioException("Failed to parse JPEG from bytes: " + FolioNative.lastError());
        long elem = FolioNative.imageElementNew(img);
        if (elem == 0) throw new FolioException("Failed to create image element: " + FolioNative.lastError());
        return new Image(img, elem);
    }

    /**
     * Loads a TIFF image from the given file path.
     *
     * @param path absolute path to the {@code .tif} / {@code .tiff} file
     * @return a new {@link Image} ready to be added to a document
     * @throws FolioException if the file cannot be read or is not a valid TIFF
     */
    public static Image loadTiff(String path) {
        long img = FolioNative.imageLoadTiff(path);
        if (img == 0) throw new FolioIOException("Failed to load TIFF: " + path + ": " + FolioNative.lastError());
        long elem = FolioNative.imageElementNew(img);
        if (elem == 0) throw new FolioException("Failed to create image element: " + FolioNative.lastError());
        return new Image(img, elem);
    }

    /**
     * Parses a PNG image from raw bytes.
     *
     * @param data the raw PNG bytes
     * @return a new {@link Image} ready to be added to a document
     * @throws FolioException if the bytes are not valid PNG data
     */
    public static Image parsePng(byte[] data) {
        long img = FolioNative.imageParsePng(data);
        if (img == 0) throw new FolioException("Failed to parse PNG from bytes: " + FolioNative.lastError());
        long elem = FolioNative.imageElementNew(img);
        if (elem == 0) throw new FolioException("Failed to create image element: " + FolioNative.lastError());
        return new Image(img, elem);
    }

    /**
     * Sets the rendered size of the image in the document.
     *
     * @param width  display width in points
     * @param height display height in points
     * @return this image, for chaining
     */
    public Image size(double width, double height) {
        FolioNative.imageElementSetSize(elementHandle.get(), width, height);
        return this;
    }

    /**
     * Sets the horizontal alignment of the image on the page.
     *
     * @param align the desired {@link Align} value
     * @return this image, for chaining
     */
    public Image align(Align align) {
        FolioNative.imageElementSetAlign(elementHandle.get(), align.value());
        return this;
    }

    /**
     * Sets alternative text for PDF/UA accessibility.
     *
     * @param text description of this image for screen readers
     * @return this image, for chaining
     */
    public Image altText(String text) {
        FolioNative.imageElementSetAltText(elementHandle.get(), text);
        return this;
    }

    /**
     * Sets the CSS-style {@code object-fit} behaviour for this image.
     *
     * @param fit one of {@code "contain"}, {@code "cover"}, {@code "fill"},
     *            {@code "none"}, or {@code "scale-down"}
     * @return this image, for chaining
     */
    public Image setObjectFit(String fit) {
        FolioNative.imageElementSetObjectFit(elementHandle.get(), fit);
        return this;
    }

    /**
     * Sets the CSS-style {@code object-position} for this image
     * (e.g., {@code "center"}, {@code "top left"}).
     *
     * @param pos the position string
     * @return this image, for chaining
     */
    public Image setObjectPosition(String pos) {
        FolioNative.imageElementSetObjectPosition(elementHandle.get(), pos);
        return this;
    }

    /**
     * Returns the natural pixel width of the source image.
     *
     * @return width in pixels
     */
    public int width() {
        return FolioNative.imageWidth(imageHandle.get());
    }

    /**
     * Returns the natural pixel height of the source image.
     *
     * @return height in pixels
     */
    public int height() {
        return FolioNative.imageHeight(imageHandle.get());
    }

    /**
     * Returns the native element handle used when adding this image to a document.
     *
     * @return the native element handle value
     */
    public long handle() {
        return elementHandle.get();
    }

    long imageHandle() {
        return imageHandle.get();
    }

    /**
     * Frees the underlying native image and element handles. Called automatically
     * by try-with-resources.
     */
    @Override
    public void close() {
        elementHandle.close();
        imageHandle.close();
    }
}
