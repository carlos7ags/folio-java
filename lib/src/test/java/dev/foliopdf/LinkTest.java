package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    @Test
    void linkTextExtractable(@TempDir Path tempDir) {
        Path output = tempDir.resolve("link.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Click the link below:"));
            doc.add(Link.of("Visit Example Site", "https://example.com")
                .color(Color.BLUE)
                .underline());
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Click the link"), "Should contain surrounding text");
            assertTrue(text.contains("Visit Example Site"), "Should contain link text");
        }
    }

    @Test
    void internalLink(@TempDir Path tempDir) {
        Path output = tempDir.resolve("internal-link.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Link.internal("Go to section", "section1", Font.helvetica(), 12)
                .color(Color.RED));
            doc.add(AreaBreak.of());
            doc.add(Paragraph.of("Section 1 content."));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(2, reader.pageCount());
            assertTrue(reader.extractText(0).contains("Go to section"));
            assertTrue(reader.extractText(1).contains("Section 1"));
        }
    }
}
