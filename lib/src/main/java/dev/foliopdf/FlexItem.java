package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A wrapper around a child element inside a {@link Flex} container, providing per-item flex
 * layout controls such as grow factor, shrink factor, basis, and self-alignment.
 *
 * <p>Instances are obtained from {@link Flex#addItem} overloads rather than constructed directly.
 *
 * <pre>{@code
 * var flex = Flex.of().direction(FlexDirection.ROW);
 * flex.addItem(Paragraph.of("Grow me", Font.helvetica(), 12)).grow(1);
 * flex.addItem(Paragraph.of("Fixed", Font.helvetica(), 12)).basis(120);
 * doc.add(flex);
 * }</pre>
 */
public final class FlexItem {

    private final HandleRef handle;

    FlexItem(long handle) {
        this.handle = new HandleRef(handle, FolioNative::flexItemFree);
    }

    /**
     * Sets the flex-grow factor, controlling how much this item expands to fill available space.
     *
     * @param grow the grow factor (0 = do not grow)
     * @return this instance for chaining
     */
    public FlexItem grow(double grow) {
        FolioNative.flexItemSetGrow(handle.get(), grow);
        return this;
    }

    /**
     * Sets the flex-shrink factor, controlling how much this item contracts when space is limited.
     *
     * @param shrink the shrink factor (0 = do not shrink)
     * @return this instance for chaining
     */
    public FlexItem shrink(double shrink) {
        FolioNative.flexItemSetShrink(handle.get(), shrink);
        return this;
    }

    /**
     * Sets the flex-basis, specifying the initial main-axis size of this item in points.
     *
     * @param basis the base size in points
     * @return this instance for chaining
     */
    public FlexItem basis(double basis) {
        FolioNative.flexItemSetBasis(handle.get(), basis);
        return this;
    }

    /**
     * Overrides the container's {@code alignItems} setting for this individual item.
     *
     * @param align the cross-axis alignment for this item
     * @return this instance for chaining
     */
    public FlexItem alignSelf(AlignItems align) {
        FolioNative.flexItemSetAlignSelf(handle.get(), align.value());
        return this;
    }

    /**
     * Sets individual margins around this flex item.
     *
     * @param top    top margin in points
     * @param right  right margin in points
     * @param bottom bottom margin in points
     * @param left   left margin in points
     * @return this instance for chaining
     */
    public FlexItem margins(double top, double right, double bottom, double left) {
        FolioNative.flexItemSetMargins(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Returns the native handle for this flex item.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
