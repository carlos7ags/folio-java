package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ColumnsTest {

    @Test
    void twoColumnLayoutPreservesText(@TempDir Path tempDir) {
        Path output = tempDir.resolve("columns.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var cols = Columns.of(2).gap(20);
            cols.add(0, Paragraph.of("Left column text here."));
            cols.add(1, Paragraph.of("Right column text here."));
            doc.add(cols);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Left column"), "Should contain left column text");
            assertTrue(text.contains("Right column"), "Should contain right column text");
        }
    }

    @Test
    void threeColumnsWithWidths(@TempDir Path tempDir) {
        Path output = tempDir.resolve("columns3.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var cols = Columns.of(3)
                .gap(10)
                .widths(100, 200, 100);
            cols.add(0, Paragraph.of("Narrow A"));
            cols.add(1, Paragraph.of("Wide middle"));
            cols.add(2, Paragraph.of("Narrow B"));
            doc.add(cols);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Narrow A"));
            assertTrue(text.contains("Wide middle"));
            assertTrue(text.contains("Narrow B"));
        }
    }
}
