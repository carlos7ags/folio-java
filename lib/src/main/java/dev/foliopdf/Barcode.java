package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A barcode element that renders QR codes, Code 128, or EAN-13 barcodes into a PDF document.
 *
 * <p>Implements {@link AutoCloseable}; use try-with-resources to ensure native handles are freed
 * promptly.
 *
 * <pre>{@code
 * try (var barcode = Barcode.qr("https://example.com", 72)) {
 *     barcode.align(Align.CENTER);
 *     doc.add(barcode);
 * }
 * }</pre>
 */
public final class Barcode implements AutoCloseable, Element {

    private final HandleRef barcodeHandle;
    private final HandleRef elementHandle;

    private Barcode(long bcHandle, long elemHandle) {
        this.barcodeHandle = new HandleRef(bcHandle, FolioNative::barcodeFree);
        this.elementHandle = new HandleRef(elemHandle, FolioNative::barcodeElementFree);
    }

    /**
     * Creates a QR code barcode element with the default error correction level.
     *
     * @param data  the data to encode
     * @param width the rendered width in points
     * @return a new {@code Barcode} instance
     * @throws FolioException if the barcode cannot be created
     */
    public static Barcode qr(String data, double width) {
        long bc = FolioNative.barcodeQr(data);
        if (bc == 0) throw new FolioException("Failed to create QR barcode: " + FolioNative.lastError());
        long elem = FolioNative.barcodeElementNew(bc, width);
        if (elem == 0) throw new FolioException("Failed to create barcode element: " + FolioNative.lastError());
        return new Barcode(bc, elem);
    }

    /**
     * Creates a QR code barcode element with the specified error correction level.
     *
     * @param data  the data to encode
     * @param width the rendered width in points
     * @param ecc   the QR error correction level
     * @return a new {@code Barcode} instance
     * @throws FolioException if the barcode cannot be created
     */
    public static Barcode qr(String data, double width, ECCLevel ecc) {
        long bc = FolioNative.barcodeQrEcc(data, ecc.value());
        if (bc == 0) throw new FolioException("Failed to create QR barcode: " + FolioNative.lastError());
        long elem = FolioNative.barcodeElementNew(bc, width);
        if (elem == 0) throw new FolioException("Failed to create barcode element: " + FolioNative.lastError());
        return new Barcode(bc, elem);
    }

    /**
     * Creates a Code 128 barcode element.
     *
     * @param data  the data to encode
     * @param width the rendered width in points
     * @return a new {@code Barcode} instance
     * @throws FolioException if the barcode cannot be created
     */
    public static Barcode code128(String data, double width) {
        long bc = FolioNative.barcodeCode128(data);
        if (bc == 0) throw new FolioException("Failed to create Code128 barcode: " + FolioNative.lastError());
        long elem = FolioNative.barcodeElementNew(bc, width);
        if (elem == 0) throw new FolioException("Failed to create barcode element: " + FolioNative.lastError());
        return new Barcode(bc, elem);
    }

    /**
     * Creates an EAN-13 barcode element.
     *
     * @param data  the 13-digit EAN-13 string to encode
     * @param width the rendered width in points
     * @return a new {@code Barcode} instance
     * @throws FolioException if the barcode cannot be created
     */
    public static Barcode ean13(String data, double width) {
        long bc = FolioNative.barcodeEan13(data);
        if (bc == 0) throw new FolioException("Failed to create EAN-13 barcode: " + FolioNative.lastError());
        long elem = FolioNative.barcodeElementNew(bc, width);
        if (elem == 0) throw new FolioException("Failed to create barcode element: " + FolioNative.lastError());
        return new Barcode(bc, elem);
    }

    /**
     * Sets the rendered height of the barcode element in points.
     *
     * @param height the height in points
     * @return this instance for chaining
     */
    public Barcode height(double height) {
        FolioNative.barcodeElementSetHeight(elementHandle.get(), height);
        return this;
    }

    /**
     * Sets the horizontal alignment of the barcode within its container.
     *
     * @param align the desired alignment
     * @return this instance for chaining
     */
    public Barcode align(Align align) {
        FolioNative.barcodeElementSetAlign(elementHandle.get(), align.value());
        return this;
    }

    /** Sets alternative text for PDF/UA accessibility. */
    public Barcode altText(String text) {
        FolioNative.barcodeElementSetAltText(elementHandle.get(), text);
        return this;
    }

    /**
     * Returns the intrinsic symbol width of the underlying barcode data (in barcode units).
     *
     * @return the barcode symbol width
     */
    public int barcodeWidth() {
        return FolioNative.barcodeWidth(barcodeHandle.get());
    }

    /**
     * Returns the intrinsic symbol height of the underlying barcode data (in barcode units).
     *
     * @return the barcode symbol height
     */
    public int barcodeHeight() {
        return FolioNative.barcodeHeight(barcodeHandle.get());
    }

    /**
     * Returns the native element handle used when adding this barcode to a document.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return elementHandle.get();
    }

    /**
     * Releases native resources held by this barcode.
     *
     * <p>Called automatically when used in a try-with-resources block.
     */
    @Override
    public void close() {
        elementHandle.close();
        barcodeHandle.close();
    }
}
