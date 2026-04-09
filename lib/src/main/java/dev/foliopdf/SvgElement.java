package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * An SVG image element that renders vector graphics into a PDF document.
 *
 * <p>Implements {@link AutoCloseable}; use try-with-resources to ensure native handles are freed
 * promptly.
 *
 * <pre>{@code
 * try (var svg = SvgElement.parse("<svg>...</svg>")) {
 *     svg.size(100, 100).align(Align.CENTER);
 *     doc.add(svg);
 * }
 * }</pre>
 */
public final class SvgElement implements AutoCloseable, Element {

    private final HandleRef svgHandle;
    private final HandleRef elementHandle;

    private SvgElement(long svgHandle, long elemHandle) {
        this.svgHandle = new HandleRef(svgHandle, FolioNative::svgFree);
        this.elementHandle = new HandleRef(elemHandle, FolioNative::svgElementFree);
    }

    /**
     * Parses an SVG document from an XML string and creates a document element.
     *
     * @param svgXml the SVG XML content as a string
     * @return a new {@code SvgElement} instance
     * @throws FolioException if the SVG cannot be parsed
     */
    public static SvgElement parse(String svgXml) {
        long svg = FolioNative.svgParse(svgXml);
        if (svg == 0) throw new FolioException("Failed to parse SVG: " + FolioNative.lastError());
        long elem = FolioNative.svgElementNew(svg);
        if (elem == 0) throw new FolioException("Failed to create SVG element: " + FolioNative.lastError());
        return new SvgElement(svg, elem);
    }

    /**
     * Parses an SVG document from raw bytes and creates a document element.
     *
     * @param data the SVG content as a byte array
     * @return a new {@code SvgElement} instance
     * @throws FolioException if the SVG cannot be parsed
     */
    public static SvgElement parseBytes(byte[] data) {
        long svg = FolioNative.svgParseBytes(data);
        if (svg == 0) throw new FolioException("Failed to parse SVG from bytes: " + FolioNative.lastError());
        long elem = FolioNative.svgElementNew(svg);
        if (elem == 0) throw new FolioException("Failed to create SVG element: " + FolioNative.lastError());
        return new SvgElement(svg, elem);
    }

    /**
     * Sets the rendered dimensions of this SVG element in points.
     *
     * @param width  the desired width in points
     * @param height the desired height in points
     * @return this instance for chaining
     */
    public SvgElement size(double width, double height) {
        FolioNative.svgElementSetSize(elementHandle.get(), width, height);
        return this;
    }

    /**
     * Sets the horizontal alignment of this SVG element within its container.
     *
     * @param align the desired alignment
     * @return this instance for chaining
     */
    public SvgElement align(Align align) {
        FolioNative.svgElementSetAlign(elementHandle.get(), align.value());
        return this;
    }

    /** Sets alternative text for PDF/UA accessibility. */
    public SvgElement altText(String text) {
        FolioNative.svgElementSetAltText(elementHandle.get(), text);
        return this;
    }

    /**
     * Returns the intrinsic width declared in the SVG source, in points.
     *
     * @return the SVG's natural width
     */
    public double svgWidth() {
        return FolioNative.svgWidth(svgHandle.get());
    }

    /**
     * Returns the intrinsic height declared in the SVG source, in points.
     *
     * @return the SVG's natural height
     */
    public double svgHeight() {
        return FolioNative.svgHeight(svgHandle.get());
    }

    /**
     * Returns the native element handle used when adding this SVG to a document.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return elementHandle.get();
    }

    /**
     * Releases native resources held by this SVG element.
     *
     * <p>Called automatically when used in a try-with-resources block.
     */
    @Override
    public void close() {
        elementHandle.close();
        svgHandle.close();
    }
}
