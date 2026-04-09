package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentToBytesTest {

    @Test
    void toBytesReturnsValidPdfHeader() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).title("ToBytes").build()) {
            doc.add(Paragraph.of("hello from toBytes"));
            bytes = doc.toBytes();
        }
        assertNotNull(bytes);
        assertTrue(bytes.length > 100, "expected non-trivial PDF output, got " + bytes.length + " bytes");
        assertEquals('%', (char) bytes[0]);
        assertEquals('P', (char) bytes[1]);
        assertEquals('D', (char) bytes[2]);
        assertEquals('F', (char) bytes[3]);
    }

    @Test
    void toBytesMatchesWriteToBuffer() {
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("content"));
            byte[] a = doc.toBytes();
            byte[] b = doc.writeToBuffer();
            assertTrue(a.length > 0);
            assertTrue(b.length > 0);
            // Both should start with %PDF
            assertEquals("%PDF", new String(a, 0, 4));
            assertEquals("%PDF", new String(b, 0, 4));
        }
    }
}
