package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

/**
 * Utility for parsing CSS length strings (e.g., {@code "1in"}, {@code "16px"},
 * {@code "50%"}, {@code "2em"}) to PDF points.
 *
 * <pre>{@code
 * double pts = CssLength.parse("1in", 12, 0);   // 72.0
 * double em  = CssLength.parse("2em", 16, 0);   // 32.0
 * double pct = CssLength.parse("50%", 12, 100); // 50.0
 * }</pre>
 */
public final class CssLength {

    private CssLength() {}

    /**
     * Parses a CSS length string and returns its value in points.
     *
     * @param css        the CSS length expression
     * @param fontSize   the current font size in points (used for {@code em}/{@code rem})
     * @param relativeTo the reference length in points (used for {@code %})
     * @return the parsed length in points
     */
    public static double parse(String css, double fontSize, double relativeTo) {
        return FolioNative.htmlParseCssLength(css, fontSize, relativeTo);
    }
}
