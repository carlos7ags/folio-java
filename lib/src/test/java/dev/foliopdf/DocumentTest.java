package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void createDocumentAndSave(@TempDir Path tempDir) {
        Path output = tempDir.resolve("basic.pdf");

        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title("Test Document")
                .build()) {
            doc.add(Paragraph.of("Basic content."));
            doc.save(output.toString());
        }

        assertValidPdf(output);
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertEquals(612, reader.pageWidth(0), 1);
            assertEquals(792, reader.pageHeight(0), 1);
            assertEquals("Test Document", reader.title());
        }
    }

    @Test
    void createDocumentWithContent(@TempDir Path tempDir) {
        Path output = tempDir.resolve("content.pdf");

        try (var doc = Document.builder()
                .pageSize(PageSize.A4)
                .title("Q3 Report")
                .author("Finance Team")
                .margins(36, 36, 36, 36)
                .build()) {

            doc.add(Heading.of("Q3 Report", HeadingLevel.H1));
            doc.add(Paragraph.of("Revenue grew 23% year over year."));

            doc.add(Table.builder()
                .headerRow("Product", "Units", "Revenue")
                .row("Widget A", "1,200", "$48,000")
                .row("Widget B", "850", "$34,000")
                .build());

            doc.save(output.toString());
        }

        assertValidPdf(output);
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            String text = reader.extractText(0);
            assertNotNull(text);
            assertTrue(text.contains("Q3 Report"), "Should contain heading text");
            assertTrue(text.contains("Revenue grew"), "Should contain paragraph text");
            assertTrue(text.contains("Widget A"), "Should contain table data");
            assertTrue(text.contains("$48,000"), "Should contain table revenue");

            assertEquals("Q3 Report", reader.title());
            assertEquals("Finance Team", reader.author());
        }
    }

    @Test
    void writeToBuffer() {
        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .build()) {
            doc.add(Paragraph.of("Buffer content test."));
            byte[] bytes = doc.writeToBuffer();
            assertNotNull(bytes);
            assertTrue(bytes.length > 100, "PDF buffer should have substantial content");
            assertPdfHeader(bytes);

            try (var reader = PdfReader.parse(bytes)) {
                assertEquals(1, reader.pageCount());
                String text = reader.extractText(0);
                assertTrue(text.contains("Buffer content"), "Round-trip buffer should preserve text");
            }
        }
    }

    @Test
    void createDocumentWithDiv(@TempDir Path tempDir) {
        Path output = tempDir.resolve("div.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var div = Div.of()
                .padding(10)
                .background(Color.LIGHT_GRAY)
                .border(1, Color.BLACK);
            div.add(Paragraph.of("Inside a div container."));
            doc.add(div);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("Inside a div"), "Should contain div paragraph text");
        }
    }

    @Test
    void createDocumentWithList(@TempDir Path tempDir) {
        Path output = tempDir.resolve("list.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(ListElement.of()
                .style(ListStyle.BULLET)
                .item("First item")
                .item("Second item")
                .item("Third item"));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("First item"), "Should contain first list item");
            assertTrue(text.contains("Second item"), "Should contain second list item");
            assertTrue(text.contains("Third item"), "Should contain third list item");
        }
    }

    @Test
    void areaBreakCreatesMultiplePages(@TempDir Path tempDir) {
        Path output = tempDir.resolve("breaks.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Page one content."));
            doc.add(AreaBreak.of());
            doc.add(Paragraph.of("Page two content."));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(2, reader.pageCount(), "Area break should create two pages");
            assertTrue(reader.extractText(0).contains("Page one"), "Page 1 should have page one text");
            assertTrue(reader.extractText(1).contains("Page two"), "Page 2 should have page two text");
        }
    }

    @Test
    void styledTableCells(@TempDir Path tempDir) {
        Path output = tempDir.resolve("styled-table.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Table.builder()
                .headerRow(row -> {
                    row.addCell("Name").background(Color.LIGHT_GRAY).padding(4);
                    row.addCell("Value").background(Color.LIGHT_GRAY).padding(4);
                })
                .row(row -> {
                    row.addCell("Alpha").align(Align.LEFT);
                    row.addCell("100").align(Align.RIGHT);
                })
                .row("Beta", "200")
                .build());
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Name"), "Should contain header cell");
            assertTrue(text.contains("Alpha"), "Should contain first row data");
            assertTrue(text.contains("Beta"), "Should contain second row data");
            assertTrue(text.contains("200"), "Should contain second row value");
        }
    }

    @Test
    void paragraphStyling(@TempDir Path tempDir) {
        Path output = tempDir.resolve("styled.pdf");

        try (var doc = Document.builder().pageSize(PageSize.A4).build()) {
            doc.add(Paragraph.of("Styled paragraph with leading.")
                .align(Align.CENTER)
                .leading(1.5)
                .spaceBefore(10)
                .spaceAfter(10));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertTrue(reader.extractText(0).contains("Styled paragraph"), "Should contain styled text");
            assertEquals(595, reader.pageWidth(0), 1);
        }
    }

    @Test
    void watermarkApplied(@TempDir Path tempDir) {
        Path output = tempDir.resolve("watermark.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.watermark("DRAFT");
            doc.add(Paragraph.of("Document with watermark."));
            doc.save(output.toString());
        }

        assertValidPdf(output);
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
        }
    }

    @Test
    void addHtmlContent(@TempDir Path tempDir) {
        Path output = tempDir.resolve("html-inline.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Before HTML."));
            doc.addHtml("<p>Inline HTML paragraph.</p>");
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Before HTML"), "Should have pre-HTML text");
            assertTrue(text.contains("Inline HTML"), "Should have inline HTML text");
        }
    }

    @Test
    void headerAndFooterCallbacks(@TempDir Path tempDir) {
        Path output = tempDir.resolve("header-footer.pdf");
        var font = Font.helvetica();

        try (var doc = Document.builder().pageSize(PageSize.LETTER)
                .margins(72, 36, 72, 36).build()) {
            doc.header((pageIndex, totalPages, page) -> {
                page.addText("CONFIDENTIAL HEADER", font, 10, 36, 20);
            });
            doc.footer((pageIndex, totalPages, page) -> {
                String footerText = "Page " + (pageIndex + 1) + " of " + totalPages;
                page.addText(footerText, font, 10, 36, 770);
            });
            doc.add(Paragraph.of("Body content on page one."));
            doc.add(AreaBreak.of());
            doc.add(Paragraph.of("Body content on page two."));
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(2, reader.pageCount());
            String page1 = reader.extractText(0);
            assertTrue(page1.contains("CONFIDENTIAL HEADER"), "Page 1 should have header text");
            assertTrue(page1.contains("Page 1 of 2"), "Page 1 should have footer with page number");
            assertTrue(page1.contains("Body content on page one"), "Page 1 should have body text");

            String page2 = reader.extractText(1);
            assertTrue(page2.contains("CONFIDENTIAL HEADER"), "Page 2 should have header text");
            assertTrue(page2.contains("Page 2 of 2"), "Page 2 should have footer with page number");
            assertTrue(page2.contains("Body content on page two"), "Page 2 should have body text");
        }
    }

    private static void assertValidPdf(Path path) {
        assertDoesNotThrow(() -> {
            byte[] bytes = Files.readAllBytes(path);
            assertTrue(bytes.length > 0, "PDF should not be empty");
            assertPdfHeader(bytes);
        });
    }

    private static void assertPdfHeader(byte[] bytes) {
        String header = new String(bytes, 0, Math.min(5, bytes.length));
        assertTrue(header.startsWith("%PDF"), "Should start with %PDF header");
    }
}
