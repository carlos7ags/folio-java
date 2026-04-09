package dev.foliopdf;

/**
 * Represents a page size as width and height in PDF points (1 pt = 1/72 inch).
 * Common sizes are provided as constants: {@link #LETTER}, {@link #A4},
 * {@link #LEGAL}, and {@link #TABLOID}. Custom sizes can be created with
 * {@link #of(double, double)}.
 *
 * <pre>{@code
 * Document.builder().pageSize(PageSize.A4).build();
 * Document.builder().pageSize(PageSize.of(500, 700)).build();
 * }</pre>
 */
public record PageSize(double width, double height) {

    public static final PageSize LETTER = new PageSize(612, 792);
    public static final PageSize A4 = new PageSize(595.28, 841.89);
    public static final PageSize LEGAL = new PageSize(612, 1008);
    public static final PageSize TABLOID = new PageSize(792, 1224);

    public static PageSize of(double width, double height) {
        return new PageSize(width, height);
    }
}
