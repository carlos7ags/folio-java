package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DivExtensionsTest {

    @Test
    void aspectRatioKeepTogetherOutlineBoxShadow() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(36).build()) {
            var div = Div.of()
                .setWidthPercent(50.0)
                .setAspectRatio(16.0 / 9.0)
                .setKeepTogether(true)
                .setBorderRadius(4, 8, 12, 16)
                .setHCenter(true)
                .setOutline(1.5, "solid", 0.2, 0.2, 0.8, 2.0)
                .addBoxShadow(2, 2, 4, 1, 0, 0, 0)
                .background(Color.of(0.9, 0.95, 1.0));
            div.add(Paragraph.of("Inside a fancy div."));
            doc.add(div);

            var right = Div.of()
                .setHRight(true)
                .setClear("both")
                .add(Paragraph.of("Right aligned"));
            doc.add(right);

            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
