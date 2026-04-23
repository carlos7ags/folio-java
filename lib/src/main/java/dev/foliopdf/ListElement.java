package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Represents an ordered or unordered list. Create instances via {@link #of()} (default
 * Helvetica 10pt) or {@link #of(Font, double)} to specify a custom font and size.
 *
 * <pre>{@code
 * doc.add(ListElement.of()
 *         .style(ListStyle.DISC)
 *         .item("First item")
 *         .item("Second item"));
 * }</pre>
 */
public final class ListElement implements Element {

    private final HandleRef handle;

    private ListElement(long handle) {
        this.handle = new HandleRef(handle, FolioNative::listFree);
    }

    /**
     * Creates a list using the specified font and font size.
     *
     * @param font     the font for list items
     * @param fontSize the font size in points
     * @return a new {@link ListElement}
     * @throws FolioException if the native call fails
     */
    public static ListElement of(Font font, double fontSize) {
        long h = FolioNative.listNew(font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create list: " + FolioNative.lastError());
        return new ListElement(h);
    }

    /**
     * Creates a list using the default Helvetica 10pt font.
     *
     * @return a new {@link ListElement}
     * @throws FolioException if the native call fails
     */
    public static ListElement of() {
        return of(Font.helvetica(), 10);
    }

    /**
     * Creates a list that embeds the font subset in the PDF output.
     *
     * @param font     the font to embed for list items
     * @param fontSize the font size in points
     * @return a new {@link ListElement} with an embedded font
     * @throws FolioException if the native call fails
     */
    public static ListElement ofEmbedded(Font font, double fontSize) {
        long h = FolioNative.listNewEmbedded(font.handle(), fontSize);
        if (h == 0) throw new FolioException("Failed to create embedded list: " + FolioNative.lastError());
        return new ListElement(h);
    }

    /**
     * Sets the bullet or numbering style for this list.
     *
     * @param style the desired {@link ListStyle}
     * @return this list, for chaining
     */
    public ListElement style(ListStyle style) {
        FolioNative.listSetStyle(handle.get(), style.value());
        return this;
    }

    /**
     * Sets the left indent for list items in points.
     *
     * @param indent indent in points
     * @return this list, for chaining
     */
    public ListElement indent(double indent) {
        FolioNative.listSetIndent(handle.get(), indent);
        return this;
    }

    /**
     * Sets the line-height multiplier for list items.
     *
     * @param leading line height as a multiple of the font size
     * @return this list, for chaining
     */
    public ListElement leading(double leading) {
        FolioNative.listSetLeading(handle.get(), leading);
        return this;
    }

    /**
     * Appends a text item to this list.
     *
     * @param text the item text
     * @return this list, for chaining
     */
    /**
     * Adds a list item with styled text runs from a {@link RunList}.
     *
     * @param runs the styled runs for this item
     * @return this list, for chaining
     */
    public ListElement itemRuns(RunList runs) {
        FolioNative.listAddItemRuns(handle.get(), runs.handle());
        return this;
    }

    /**
     * Adds a list item with styled runs and returns a nested sub-list.
     *
     * @param runs the styled runs for this item
     * @return a new {@link ListElement} representing the nested sub-list
     */
    public ListElement itemRunsWithSublist(RunList runs) {
        long h = FolioNative.listAddItemRunsWithSublist(handle.get(), runs.handle());
        if (h == 0) throw new FolioException("Failed to add item runs with sublist: " + FolioNative.lastError());
        return new ListElement(h);
    }

    public ListElement item(String text) {
        FolioNative.listAddItem(handle.get(), text);
        return this;
    }

    /**
     * Sets the writing direction (LTR, RTL, or AUTO) for this list.
     *
     * <p>RTL lists place markers (bullets or numbers) on the right and run
     * item text right-to-left. {@link Direction#AUTO} runs the Unicode Bidi
     * algorithm over the items to infer direction. See ISO 32000-2 §14.8.2
     * for how direction interacts with tagged-PDF structure attributes.
     *
     * @param direction the desired {@link Direction}
     * @return this list, for chaining
     * @since 0.7.1
     */
    public ListElement setDirection(Direction direction) {
        FolioNative.listSetDirection(handle.get(), direction.value());
        return this;
    }

    /**
     * Appends a nested (indented) sub-list item and returns the new sub-list.
     *
     * @param text the nested item text
     * @return the new nested {@link ListElement}
     * @throws FolioException if the native call fails
     */
    public ListElement nestedItem(String text) {
        long h = FolioNative.listAddNestedItem(handle.get(), text);
        if (h == 0) throw new FolioException("Failed to add nested item: " + FolioNative.lastError());
        return new ListElement(h);
    }

    /**
     * Returns the native handle for this list.
     *
     * @return the native handle value
     */
    public long handle() {
        return handle.get();
    }
}
