package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageDrawingTest {

    @Test
    void addLineRectAndRectFilled() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).build()) {
            Page page = doc.addPage();
            page.addLine(72, 72, 500, 72, 1.0, 0, 0, 0);
            page.addRect(72, 100, 200, 60, 1.5, 0, 0, 0.7);
            page.addRectFilled(72, 200, 200, 60, 0.9, 0.9, 0.2);
            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
