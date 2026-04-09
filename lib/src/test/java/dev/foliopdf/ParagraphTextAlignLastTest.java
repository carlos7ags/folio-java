package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphTextAlignLastTest {

    @Test
    void textAlignLastRendersValidPdf() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(72).build()) {
            var p = Paragraph.of(
                "The last line of a justified paragraph can be aligned left, right, or centered. "
              + "This paragraph is long enough to wrap onto multiple lines so that text-align-last has an effect.")
                .align(Align.JUSTIFY)
                .setTextAlignLast(Align.CENTER);
            doc.add(p);
            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
