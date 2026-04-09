package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A flexbox container that arranges child elements in a row or column with CSS-like flex layout.
 *
 * <p>Children can be added directly (with default flex item settings) via {@link #add} overloads,
 * or as {@link FlexItem} wrappers (via {@link #addItem} overloads) to control per-item grow,
 * shrink, and basis properties.
 *
 * <pre>{@code
 * var flex = Flex.of()
 *     .direction(FlexDirection.ROW)
 *     .gap(10)
 *     .justifyContent(JustifyContent.SPACE_BETWEEN);
 * flex.add(Paragraph.of("Left", Font.helvetica(), 12));
 * flex.add(Paragraph.of("Right", Font.helvetica(), 12));
 * doc.add(flex);
 * }</pre>
 */
public final class Flex implements Element {

    private final HandleRef handle;

    private Flex(long handle) {
        this.handle = new HandleRef(handle, FolioNative::flexFree);
    }

    /**
     * Creates a new, empty flex container with default settings.
     *
     * @return a new {@code Flex} instance
     * @throws FolioException if the native flex container cannot be created
     */
    public static Flex of() {
        long h = FolioNative.flexNew();
        if (h == 0) throw new FolioException("Failed to create flex: " + FolioNative.lastError());
        return new Flex(h);
    }

    /**
     * Adds a paragraph to this flex container using default item settings.
     *
     * @param paragraph the paragraph to add
     * @return this instance for chaining
     */
    public Flex add(Paragraph paragraph) {
        FolioNative.flexAdd(handle.get(), paragraph.handle());
        return this;
    }

    /**
     * Adds a heading to this flex container using default item settings.
     *
     * @param heading the heading to add
     * @return this instance for chaining
     */
    public Flex add(Heading heading) {
        FolioNative.flexAdd(handle.get(), heading.handle());
        return this;
    }

    /**
     * Adds an image to this flex container using default item settings.
     *
     * @param image the image to add
     * @return this instance for chaining
     */
    public Flex add(Image image) {
        FolioNative.flexAdd(handle.get(), image.handle());
        return this;
    }

    /**
     * Adds a div to this flex container using default item settings.
     *
     * @param div the div to add
     * @return this instance for chaining
     */
    public Flex add(Div div) {
        FolioNative.flexAdd(handle.get(), div.handle());
        return this;
    }

    /**
     * Adds a table to this flex container using default item settings.
     *
     * @param table the table to add
     * @return this instance for chaining
     */
    public Flex add(Table table) {
        FolioNative.flexAdd(handle.get(), table.handle());
        return this;
    }

    /** Adds any {@link Element} to this flex container. */
    public Flex add(Element element) {
        FolioNative.flexAdd(handle.get(), element.handle());
        return this;
    }

    /**
     * Adds a paragraph as a {@link FlexItem}, returning the item for per-item flex configuration.
     *
     * @param paragraph the paragraph to wrap and add
     * @return the {@link FlexItem} for this child
     */
    public FlexItem addItem(Paragraph paragraph) {
        return wrapItem(paragraph.handle());
    }

    /**
     * Adds a heading as a {@link FlexItem}, returning the item for per-item flex configuration.
     *
     * @param heading the heading to wrap and add
     * @return the {@link FlexItem} for this child
     */
    public FlexItem addItem(Heading heading) {
        return wrapItem(heading.handle());
    }

    /**
     * Adds an image as a {@link FlexItem}, returning the item for per-item flex configuration.
     *
     * @param image the image to wrap and add
     * @return the {@link FlexItem} for this child
     */
    public FlexItem addItem(Image image) {
        return wrapItem(image.handle());
    }

    /**
     * Adds a div as a {@link FlexItem}, returning the item for per-item flex configuration.
     *
     * @param div the div to wrap and add
     * @return the {@link FlexItem} for this child
     */
    public FlexItem addItem(Div div) {
        return wrapItem(div.handle());
    }

    private FlexItem wrapItem(long elementHandle) {
        long item = FolioNative.flexItemNew(elementHandle);
        if (item == 0) throw new FolioException("Failed to create flex item: " + FolioNative.lastError());
        FlexItem fi = new FlexItem(item);
        FolioNative.flexAddItem(handle.get(), item);
        return fi;
    }

    /**
     * Sets the main-axis direction for laying out children.
     *
     * @param direction {@link FlexDirection#ROW} or {@link FlexDirection#COLUMN}
     * @return this instance for chaining
     */
    public Flex direction(FlexDirection direction) {
        FolioNative.flexSetDirection(handle.get(), direction.value());
        return this;
    }

    /**
     * Sets how children are distributed along the main axis.
     *
     * @param justify the justify-content strategy
     * @return this instance for chaining
     */
    public Flex justifyContent(JustifyContent justify) {
        FolioNative.flexSetJustifyContent(handle.get(), justify.value());
        return this;
    }

    /**
     * Sets how children are aligned along the cross axis.
     *
     * @param align the align-items strategy
     * @return this instance for chaining
     */
    public Flex alignItems(AlignItems align) {
        FolioNative.flexSetAlignItems(handle.get(), align.value());
        return this;
    }

    /**
     * Controls whether children wrap onto multiple lines when they overflow.
     *
     * @param wrap {@link FlexWrap#WRAP} to allow wrapping, {@link FlexWrap#NOWRAP} to prevent it
     * @return this instance for chaining
     */
    public Flex wrap(FlexWrap wrap) {
        FolioNative.flexSetWrap(handle.get(), wrap.value());
        return this;
    }

    /**
     * Sets uniform gap between children on both axes.
     *
     * @param gap the gap in points
     * @return this instance for chaining
     */
    public Flex gap(double gap) {
        FolioNative.flexSetGap(handle.get(), gap);
        return this;
    }

    /**
     * Sets the gap between rows of wrapped children.
     *
     * @param gap the row gap in points
     * @return this instance for chaining
     */
    public Flex rowGap(double gap) {
        FolioNative.flexSetRowGap(handle.get(), gap);
        return this;
    }

    /**
     * Sets the gap between columns of children.
     *
     * @param gap the column gap in points
     * @return this instance for chaining
     */
    public Flex columnGap(double gap) {
        FolioNative.flexSetColumnGap(handle.get(), gap);
        return this;
    }

    /**
     * Sets uniform padding on all four sides of the container.
     *
     * @param padding the padding in points
     * @return this instance for chaining
     */
    public Flex padding(double padding) {
        FolioNative.flexSetPadding(handle.get(), padding);
        return this;
    }

    /**
     * Sets individual padding on each side of the container.
     *
     * @param top    top padding in points
     * @param right  right padding in points
     * @param bottom bottom padding in points
     * @param left   left padding in points
     * @return this instance for chaining
     */
    public Flex padding(double top, double right, double bottom, double left) {
        FolioNative.flexSetPaddingAll(handle.get(), top, right, bottom, left);
        return this;
    }

    /**
     * Sets the background color of the container.
     *
     * @param color the RGB background color
     * @return this instance for chaining
     */
    public Flex background(Color color) {
        FolioNative.flexSetBackground(handle.get(), color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets a border around the container.
     *
     * @param width the border line width in points
     * @param color the border color
     * @return this instance for chaining
     */
    public Flex border(double width, Color color) {
        FolioNative.flexSetBorder(handle.get(), width, color.r(), color.g(), color.b());
        return this;
    }

    /**
     * Sets extra vertical space before this container in the document flow.
     *
     * @param pts space in points
     * @return this instance for chaining
     */
    public Flex spaceBefore(double pts) {
        FolioNative.flexSetSpaceBefore(handle.get(), pts);
        return this;
    }

    /**
     * Sets extra vertical space after this container in the document flow.
     *
     * @param pts space in points
     * @return this instance for chaining
     */
    public Flex spaceAfter(double pts) {
        FolioNative.flexSetSpaceAfter(handle.get(), pts);
        return this;
    }

    /**
     * Sets how wrapped lines are distributed along the cross axis.
     *
     * @param align the align-content strategy
     * @return this instance for chaining
     */
    public Flex setAlignContent(JustifyContent align) {
        FolioNative.flexSetAlignContent(handle.get(), align.value());
        return this;
    }

    /**
     * Sets individual borders for each edge of the flex container.
     *
     * @param topW    top border width
     * @param topR    top border red channel
     * @param topG    top border green channel
     * @param topB    top border blue channel
     * @param rightW  right border width
     * @param rightR  right border red channel
     * @param rightG  right border green channel
     * @param rightB  right border blue channel
     * @param bottomW bottom border width
     * @param bottomR bottom border red channel
     * @param bottomG bottom border green channel
     * @param bottomB bottom border blue channel
     * @param leftW   left border width
     * @param leftR   left border red channel
     * @param leftG   left border green channel
     * @param leftB   left border blue channel
     * @return this instance for chaining
     */
    public Flex setBorders(double topW, double topR, double topG, double topB,
                           double rightW, double rightR, double rightG, double rightB,
                           double bottomW, double bottomR, double bottomG, double bottomB,
                           double leftW, double leftR, double leftG, double leftB) {
        FolioNative.flexSetBorders(handle.get(),
            topW, topR, topG, topB,
            rightW, rightR, rightG, rightB,
            bottomW, bottomR, bottomG, bottomB,
            leftW, leftR, leftG, leftB);
        return this;
    }

    /**
     * Returns the native handle for this flex container.
     *
     * @return the opaque native handle value
     */
    public long handle() {
        return handle.get();
    }
}
