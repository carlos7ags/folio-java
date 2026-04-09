package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CssLengthTest {

    @Test
    void parsesInchesToPoints() {
        assertEquals(72.0, CssLength.parse("1in", 12, 0), 0.01);
    }

    @Test
    void parsesPixelsToPoints() {
        // 16px at 96 dpi == 12pt
        assertEquals(12.0, CssLength.parse("16px", 12, 0), 0.01);
    }

    @Test
    void parsesEmRelativeToFontSize() {
        assertEquals(32.0, CssLength.parse("2em", 16, 0), 0.01);
    }

    @Test
    void parsesPercentRelativeToReference() {
        assertEquals(50.0, CssLength.parse("50%", 12, 100), 0.01);
    }
}
