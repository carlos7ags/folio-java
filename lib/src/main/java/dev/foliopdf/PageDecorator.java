package dev.foliopdf;

/**
 * Callback invoked for each page to render a repeating header or footer.
 * Register implementations via {@link Document#header(PageDecorator)} or
 * {@link Document#footer(PageDecorator)}.
 *
 * <pre>{@code
 * doc.footer((pageIndex, totalPages, page) ->
 *     page.addText("Page " + (pageIndex + 1) + " of " + totalPages,
 *                  Font.helvetica(), 9, 72, 24));
 * }</pre>
 */
@FunctionalInterface
public interface PageDecorator {
    /**
     * Decorates a single page with header or footer content.
     *
     * @param pageIndex  zero-based index of the current page
     * @param totalPages total number of pages in the document
     * @param page       the {@link Page} to draw on
     */
    void decorate(int pageIndex, int totalPages, Page page);
}
