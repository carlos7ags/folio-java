package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Smoke tests for the v0.7.0 {@link Direction} setters on Paragraph, List, and Table. */
class DirectionTest {

    @Test
    void directionEnumValuesMatchCMacros() {
        assertEquals(0, Direction.AUTO.value());
        assertEquals(1, Direction.LTR.value());
        assertEquals(2, Direction.RTL.value());
    }

    @Test
    void paragraphDirectionAcceptsAllValues(@TempDir Path tempDir) {
        Path output = tempDir.resolve("para-rtl.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("English content").setDirection(Direction.LTR));
            doc.add(Paragraph.of("Auto-detected").setDirection(Direction.AUTO));
            doc.add(Paragraph.of("שלום").setDirection(Direction.RTL));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("English content"));
        }
    }

    @Test
    void listDirectionAcceptsAllValues(@TempDir Path tempDir) {
        Path output = tempDir.resolve("list-rtl.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(ListElement.of()
                .setDirection(Direction.RTL)
                .item("אלף")
                .item("בית"));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
        }
    }

    @Test
    void tableDirectionAcceptsAllValues(@TempDir Path tempDir) {
        Path output = tempDir.resolve("table-rtl.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var table = Table.builder()
                .row("First", "Second", "Third")
                .build();
            table.setDirection(Direction.RTL);
            doc.add(table);
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            String text = reader.extractText(0);
            assertTrue(text.contains("First"));
            assertTrue(text.contains("Second"));
            assertTrue(text.contains("Third"));
        }
    }

    @Test
    void documentSetActualTextToggle(@TempDir Path tempDir) {
        Path output = tempDir.resolve("actualtext.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.tagged(true);
            doc.setActualText(false);
            doc.add(Paragraph.of("Tagged without ActualText."));
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("without ActualText"));
        }
    }

    @Test
    void columnsSetBalancedToggle(@TempDir Path tempDir) {
        Path output = tempDir.resolve("balanced-cols.pdf");
        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var cols = Columns.of(2).gap(20).setBalanced(true);
            cols.add(0, Paragraph.of("Left fragment one."));
            cols.add(0, Paragraph.of("Left fragment two."));
            cols.add(1, Paragraph.of("Right fragment."));
            doc.add(cols);
            doc.save(output.toString());
        }
        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
            String text = reader.extractText(0);
            assertTrue(text.contains("Left fragment one"));
            assertTrue(text.contains("Right fragment"));
        }
    }
}
