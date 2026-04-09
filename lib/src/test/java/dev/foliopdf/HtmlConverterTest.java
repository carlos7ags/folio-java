package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HtmlConverterTest {

    @Test
    void htmlToPdfPreservesContent(@TempDir Path tempDir) {
        Path output = tempDir.resolve("html.pdf");
        HtmlConverter.toPdf("<h1>Hello</h1><p>World of PDF</p>", output.toString());

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            String text = reader.extractText(0);
            assertTrue(text.contains("Hello"), "Should contain heading text");
            assertTrue(text.contains("World of PDF"), "Should contain paragraph text");
        }
    }

    @Test
    void htmlToBufferRoundTrip() {
        byte[] bytes = HtmlConverter.toBuffer("<h1>Invoice</h1><p>Due amount: $1,200</p>");
        assertNotNull(bytes);
        assertTrue(bytes.length > 100);
        String header = new String(bytes, 0, Math.min(5, bytes.length));
        assertTrue(header.startsWith("%PDF"));

        try (var reader = PdfReader.parse(bytes)) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Invoice"), "Buffer round-trip should contain heading");
            assertTrue(text.contains("1,200"), "Buffer round-trip should contain amount");
        }
    }

    @Test
    void htmlToDocumentAllowsModification(@TempDir Path tempDir) {
        Path output = tempDir.resolve("html-doc.pdf");

        try (var doc = HtmlConverter.toDocument("<p>From HTML.</p>")) {
            doc.add(Paragraph.of("Appended after HTML."));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("From HTML"), "Should contain HTML-originated text");
            assertTrue(text.contains("Appended after"), "Should contain appended text");
        }
    }
}
