package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke tests for the v0.7.0 {@link WriteOptions} writer-optimizer bindings. */
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
}
