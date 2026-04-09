package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfReaderTest {

    @Test
    void readPdfPageCountAndDimensions(@TempDir Path tempDir) {
        Path pdf = tempDir.resolve("test.pdf");

        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title("Reader Test")
                .author("Test Author")
                .build()) {
            doc.add(Paragraph.of("Hello, world!"));
            doc.save(pdf.toString());
        }

        try (var reader = PdfReader.open(pdf.toString())) {
            assertEquals(1, reader.pageCount());
            assertEquals(612, reader.pageWidth(0), 1);
            assertEquals(792, reader.pageHeight(0), 1);
        }
    }

    @Test
    void readPdfMetadataRoundTrip(@TempDir Path tempDir) {
        Path pdf = tempDir.resolve("meta.pdf");

        try (var doc = Document.builder()
                .title("My Title")
                .author("My Author")
                .build()) {
            doc.add(Paragraph.of("Content"));
            doc.save(pdf.toString());
        }

        try (var reader = PdfReader.open(pdf.toString())) {
            assertEquals("My Title", reader.title());
            assertEquals("My Author", reader.author());
            assertNotNull(reader.pdfVersion());
        }
    }

    @Test
    void extractTextVerifiesContent(@TempDir Path tempDir) {
        Path pdf = tempDir.resolve("extract.pdf");

        try (var doc = Document.builder().build()) {
            doc.add(Paragraph.of("Extractable text content."));
            doc.add(Paragraph.of("Second paragraph here."));
            doc.save(pdf.toString());
        }

        try (var reader = PdfReader.open(pdf.toString())) {
            String text = reader.extractText(0);
            assertNotNull(text);
            assertTrue(text.contains("Extractable text"), "Should extract first paragraph");
            assertTrue(text.contains("Second paragraph"), "Should extract second paragraph");
        }
    }

    @Test
    void parseFromBytes(@TempDir Path tempDir) {
        Path pdf = tempDir.resolve("bytes.pdf");

        try (var doc = Document.builder()
                .title("Bytes Test")
                .build()) {
            doc.add(Paragraph.of("Parsed from byte array."));
            doc.save(pdf.toString());
        }

        assertDoesNotThrow(() -> {
            byte[] bytes = Files.readAllBytes(pdf);
            try (var reader = PdfReader.parse(bytes)) {
                assertEquals(1, reader.pageCount());
                assertEquals("Bytes Test", reader.title());
                assertTrue(reader.extractText(0).contains("Parsed from byte"),
                    "Should extract text from byte-parsed PDF");
            }
        });
    }

    @Test
    void multiPageDocument(@TempDir Path tempDir) {
        Path pdf = tempDir.resolve("multi.pdf");

        try (var doc = Document.builder().build()) {
            doc.add(Paragraph.of("First page text."));
            doc.add(AreaBreak.of());
            doc.add(Paragraph.of("Second page text."));
            doc.add(AreaBreak.of());
            doc.add(Paragraph.of("Third page text."));
            doc.save(pdf.toString());
        }

        try (var reader = PdfReader.open(pdf.toString())) {
            assertEquals(3, reader.pageCount());
            assertTrue(reader.extractText(0).contains("First page"));
            assertTrue(reader.extractText(1).contains("Second page"));
            assertTrue(reader.extractText(2).contains("Third page"));
        }
    }
}
