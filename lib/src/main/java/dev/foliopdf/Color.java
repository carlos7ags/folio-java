package dev.foliopdf;

/**
 * Represents an RGB color with each channel in the range {@code [0.0, 1.0]}.
 * Common colors are available as constants. New colors can be created from
 * normalised floats ({@link #of}), 8-bit integers ({@link #rgb}), or a
 * CSS hex string ({@link #hex}).
 *
 * <pre>{@code
 * Color red   = Color.RED;
 * Color coral = Color.rgb(255, 127, 80);
 * Color navy  = Color.hex("#001F5B");
 * }</pre>
 */
public record Color(double r, double g, double b) {

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color GRAY = new Color(0.5, 0.5, 0.5);
    public static final Color LIGHT_GRAY = new Color(0.75, 0.75, 0.75);
    public static final Color DARK_GRAY = new Color(0.25, 0.25, 0.25);
    public static final Color NAVY = new Color(0.0, 0.0, 0.5);
    public static final Color TEAL = new Color(0.0, 0.5, 0.5);
    public static final Color ORANGE = new Color(1.0, 0.65, 0.0);
    public static final Color PURPLE = new Color(0.5, 0.0, 0.5);
    public static final Color YELLOW = new Color(1.0, 1.0, 0.0);
    public static final Color CYAN = new Color(0.0, 1.0, 1.0);
    public static final Color MAGENTA = new Color(1.0, 0.0, 1.0);
    public static final Color BROWN = new Color(0.6, 0.3, 0.0);
    public static final Color PINK = new Color(1.0, 0.75, 0.8);

    public static Color of(double r, double g, double b) {
        return new Color(r, g, b);
    }

    public static Color rgb(int r, int g, int b) {
        return new Color(r / 255.0, g / 255.0, b / 255.0);
    }

    public static Color hex(String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        return rgb(
            Integer.parseInt(h.substring(0, 2), 16),
            Integer.parseInt(h.substring(2, 4), 16),
            Integer.parseInt(h.substring(4, 6), 16)
        );
    }
}
