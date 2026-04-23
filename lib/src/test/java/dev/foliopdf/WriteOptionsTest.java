package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke tests for the v0.7.1 {@link WriteOptions} writer-optimizer bindings. */
class WriteOptionsTest {

    @Test
    void saveWithObjectStreamsProducesValidPdf(@TempDir Path tempDir) throws Exception {
        Path output = tempDir.resolve("optimized.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .objectStreamCapacity(64)
                 .orphanSweep(true)
                 .cleanContentStreams(true)
                 .deduplicateObjects(true)
                 .recompressStreams(true)
                 .build()) {
            doc.add(Paragraph.of("Optimizer output."));
            doc.saveWithOptions(output.toString(), opts);
        }

        byte[] bytes = Files.readAllBytes(output);
        assertTrue(bytes.length > 0, "PDF should not be empty");
        String header = new String(bytes, 0, Math.min(5, bytes.length));
        assertTrue(header.startsWith("%PDF"), "Should start with %PDF header");

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("Optimizer output"));
        }
    }

    @Test
    void saveWithNullOptionsUsesDefaults(@TempDir Path tempDir) throws Exception {
        Path output = tempDir.resolve("defaults.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Default writer settings."));
            doc.saveWithOptions(output.toString(), null);
        }

        byte[] bytes = Files.readAllBytes(output);
        assertTrue(bytes.length > 0);
        String header = new String(bytes, 0, Math.min(5, bytes.length));
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void toBytesWithOptionsRoundTrips() {
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .deduplicateObjects(true)
                 .build()) {
            doc.add(Paragraph.of("Buffer optimizer test."));
            byte[] bytes = doc.toBytesWithOptions(opts);
            assertNotNull(bytes);
            assertTrue(bytes.length > 100);
            String header = new String(bytes, 0, 5);
            assertTrue(header.startsWith("%PDF"));

            try (var reader = PdfReader.parse(bytes)) {
                assertEquals(1, reader.pageCount());
                assertTrue(reader.extractText(0).contains("Buffer optimizer"));
            }
        }
    }

    @Test
    void toBytesWithNullOptionsUsesDefaults() {
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Default buffer."));
            byte[] bytes = doc.toBytesWithOptions(null);
            assertNotNull(bytes);
            assertTrue(bytes.length > 0);
            String header = new String(bytes, 0, 5);
            assertTrue(header.startsWith("%PDF"));
        }
    }

    /**
     * Proves the optimizer toggles actually reach the writer: the upstream
     * "many empty pages" fixture is the shape with the largest, most stable
     * delta (page-tree-heavy, almost no content streams), so a bound binding
     * should produce a meaningfully smaller output.
     */
    @Test
    void optimizerToggleActuallyShrinksOutput() {
        byte[] defaultBytes;
        byte[] optimizedBytes;

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            for (int i = 0; i < 50; i++) doc.addPage();
            defaultBytes = doc.toBytes();
        }

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .orphanSweep(true)
                 .deduplicateObjects(true)
                 .build()) {
            for (int i = 0; i < 50; i++) doc.addPage();
            optimizedBytes = doc.toBytesWithOptions(opts);
        }

        assertTrue(optimizedBytes.length < defaultBytes.length / 2,
            "optimized output should be at least half the default size for the many-empty-pages fixture; "
            + "default=" + defaultBytes.length + " optimized=" + optimizedBytes.length);
    }

    /**
     * {@code objectStreamCapacity} is the only {@code int} setter on
     * {@link WriteOptions}. Vary the value across the FFI to protect against
     * a silent type-mismatch in the function descriptor.
     */
    @Test
    void objectStreamCapacityRoundTrips() {
        for (int capacity : new int[]{1, 16, 200}) {
            try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
                 var opts = WriteOptions.builder()
                     .useXrefStream(true)
                     .useObjectStreams(true)
                     .objectStreamCapacity(capacity)
                     .build()) {
                doc.add(Paragraph.of("Capacity " + capacity));
                doc.add(Paragraph.of("Second paragraph " + capacity));
                byte[] bytes = doc.toBytesWithOptions(opts);
                try (var reader = PdfReader.parse(bytes)) {
                    assertEquals(1, reader.pageCount(),
                        "page count should be 1 for capacity " + capacity);
                    String text = reader.extractText(0);
                    assertTrue(text.contains("Capacity " + capacity),
                        "extracted text missing for capacity " + capacity);
                }
            }
        }
    }

    @Test
    void saveWithOptionsAcceptsPathOverload(@TempDir Path tempDir) throws Exception {
        Path output = tempDir.resolve("path-overload.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var opts = WriteOptions.builder().useXrefStream(true).build()) {
            doc.add(Paragraph.of("Path overload."));
            doc.saveWithOptions(output, opts);
        }
        assertTrue(Files.size(output) > 0);
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("Path overload"));
        }
    }
}
