package dev.foliopdf;

/**
 * Marker interface for layout elements that can be added to a
 * {@link Document}, {@link Div}, {@link Flex}, {@link Grid}, or {@link Columns}.
 *
 * <p>All concrete elements (Paragraph, Heading, Table, Image, etc.)
 * implement this interface, enabling a single {@code add(Element)} method
 * alongside the type-specific overloads.
 */
public interface Element {

    /**
     * Returns the native handle for this element. Used internally when
     * adding the element to a container.
     *
     * @return the opaque native handle value
     */
    long handle();
}
