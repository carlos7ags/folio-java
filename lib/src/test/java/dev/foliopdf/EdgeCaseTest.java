package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EdgeCaseTest {

    @Test
    void doubleCloseDocument() {
        var doc = Document.builder().build();
        doc.add(Paragraph.of("content"));
        doc.close();
        assertDoesNotThrow(doc::close, "Double close should not throw");
    }

    @Test
    void doubleCloseFont() {
        var font = Font.loadTTF(findSystemFont());
        if (font == null) return;
        font.close();
        assertDoesNotThrow(font::close, "Double close should not throw");
    }

    @Test
    void writeEmptyDocumentToBuffer() {
        try (var doc = Document.builder().letter().build()) {
            byte[] bytes = doc.writeToBuffer();
            assertNotNull(bytes);
            // Empty doc may produce a valid PDF or may be empty — just shouldn't crash
        }
    }

    @Test
    void errorCodeStructured() {
        assertEquals(ErrorCode.IO, ErrorCode.fromCode(-3));
        assertEquals(ErrorCode.HANDLE, ErrorCode.fromCode(-1));
        assertEquals(ErrorCode.ARG, ErrorCode.fromCode(-2));
        assertEquals(ErrorCode.PDF, ErrorCode.fromCode(-4));
        assertEquals(ErrorCode.TYPE, ErrorCode.fromCode(-5));
        assertEquals(ErrorCode.INTERNAL, ErrorCode.fromCode(-6));
        assertEquals(ErrorCode.NONE, ErrorCode.fromCode(0));
        assertEquals(ErrorCode.NONE, ErrorCode.fromCode(999));
    }

    @Test
    void exceptionCarriesErrorCode() {
        var ex = new FolioException(-3, "test io error");
        assertEquals(-3, ex.errorCode());
        assertEquals(ErrorCode.IO, ex.error());
        assertTrue(ex.getMessage().contains("test io error"));
    }

    @Test
    void convenienceA4Builder(@TempDir Path tempDir) {
        Path output = tempDir.resolve("a4.pdf");
        try (var doc = Document.builder().a4().title("A4 Test").margins(36).build()) {
            doc.add(Paragraph.of("A4 with uniform margins."));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(595, reader.pageWidth(0), 1);
            assertEquals("A4 Test", reader.title());
        }
    }

    @Test
    void convenienceLetterBuilder(@TempDir Path tempDir) {
        Path output = tempDir.resolve("letter.pdf");
        try (var doc = Document.builder().letter().build()) {
            doc.add(Paragraph.of("Letter size."));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(612, reader.pageWidth(0), 1);
        }
    }

    @Test
    void paragraphFontSizeOnly(@TempDir Path tempDir) {
        Path output = tempDir.resolve("fontsize.pdf");
        try (var doc = Document.builder().letter().build()) {
            doc.add(Paragraph.of("Small text.", 8));
            doc.add(Paragraph.of("Large text.", 24));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Small text"));
            assertTrue(text.contains("Large text"));
        }
    }

    @Test
    void elementInterfaceGenericAdd(@TempDir Path tempDir) {
        Path output = tempDir.resolve("element.pdf");
        try (var doc = Document.builder().letter().build()) {
            Element[] elements = {
                Paragraph.of("Paragraph via Element interface."),
                Heading.of("Heading via Element", HeadingLevel.H2),
                LineSeparator.of(),
            };
            for (Element e : elements) {
                doc.add(e);
            }
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Paragraph via Element"));
            assertTrue(text.contains("Heading via Element"));
        }
    }

    @Test
    void elementInterfaceInDiv(@TempDir Path tempDir) {
        Path output = tempDir.resolve("div-element.pdf");
        try (var doc = Document.builder().letter().build()) {
            var div = Div.of().padding(10);
            Element p = Paragraph.of("Inside div via Element.");
            div.add(p);
            doc.add(div);
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertTrue(reader.extractText(0).contains("Inside div via Element"));
        }
    }

    @Test
    void concurrentDocumentCreation(@TempDir Path tempDir) throws InterruptedException {
        int threadCount = 8;
        var latch = new CountDownLatch(threadCount);
        var errors = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            Thread.ofVirtual().start(() -> {
                try {
                    Path output = tempDir.resolve("concurrent-" + idx + ".pdf");
                    try (var doc = Document.builder().letter()
                            .title("Thread " + idx).build()) {
                        doc.add(Paragraph.of("Content from thread " + idx));
                        doc.add(Table.builder()
                            .headerRow("Col A", "Col B")
                            .row("val-" + idx, String.valueOf(idx * 100))
                            .build());
                        doc.save(output.toString());
                    }
                    // Verify round-trip
                    try (var reader = PdfReader.open(output.toString())) {
                        String text = reader.extractText(0);
                        if (!text.contains("thread " + idx)) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    errors.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertEquals(0, errors.get(), "All concurrent threads should produce valid PDFs");

        // Verify all files exist
        for (int i = 0; i < threadCount; i++) {
            assertTrue(Files.exists(tempDir.resolve("concurrent-" + i + ".pdf")));
        }
    }

    @Test
    void pdfReaderParseInvalidData() {
        assertThrows(FolioException.class, () ->
            PdfReader.parse(new byte[]{1, 2, 3, 4, 5}),
            "Parsing garbage should throw");
    }

    @Test
    void mergerRoundTrip(@TempDir Path tempDir) {
        Path pdf1 = tempDir.resolve("m1.pdf");
        Path pdf2 = tempDir.resolve("m2.pdf");
        Path merged = tempDir.resolve("merged.pdf");

        try (var doc = Document.builder().letter().title("Doc 1").build()) {
            doc.add(Paragraph.of("First document content."));
            doc.save(pdf1.toString());
        }
        try (var doc = Document.builder().letter().title("Doc 2").build()) {
            doc.add(Paragraph.of("Second document content."));
            doc.save(pdf2.toString());
        }

        try (var r1 = PdfReader.open(pdf1.toString());
             var r2 = PdfReader.open(pdf2.toString());
             var m = PdfMerger.merge(r1, r2)) {
            m.setInfo("Merged", "Test");
            m.save(merged.toString());
        }

        try (var reader = PdfReader.open(merged.toString())) {
            assertEquals(2, reader.pageCount());
            assertTrue(reader.extractText(0).contains("First document"));
            assertTrue(reader.extractText(1).contains("Second document"));
            assertEquals("Merged", reader.title());
        }
    }

    @Test
    void runListWithHeading(@TempDir Path tempDir) {
        Path output = tempDir.resolve("runs.pdf");
        try (var doc = Document.builder().letter().build()) {
            var heading = Heading.of("placeholder", HeadingLevel.H1);
            try (var runs = RunList.of()) {
                runs.add("Styled ", Font.helvetica(), 18, Color.BLACK)
                    .add("Heading", Font.helveticaBold(), 18, Color.RED)
                    .lastUnderline();
                heading.setRuns(runs);
            }
            doc.add(heading);
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Styled"));
            assertTrue(text.contains("Heading"));
        }
    }

    private static String findSystemFont() {
        String[] candidates = {
            "/System/Library/Fonts/Supplemental/Arial.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "C:\\Windows\\Fonts\\arial.ttf",
        };
        for (String p : candidates) {
            if (new java.io.File(p).exists()) return p;
        }
        return null;
    }
}
