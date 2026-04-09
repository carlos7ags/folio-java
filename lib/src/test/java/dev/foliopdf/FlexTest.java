package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FlexTest {

    @Test
    void flexRowLayoutPreservesText(@TempDir Path tempDir) {
        Path output = tempDir.resolve("flex-row.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var flex = Flex.of()
                .direction(FlexDirection.ROW)
                .gap(10)
                .padding(20);
            flex.add(Paragraph.of("Left side"));
            flex.add(Paragraph.of("Center part"));
            flex.add(Paragraph.of("Right side"));
            doc.add(flex);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Left side"), "Should contain left flex child text");
            assertTrue(text.contains("Center part"), "Should contain center flex child text");
            assertTrue(text.contains("Right side"), "Should contain right flex child text");
        }
    }

    @Test
    void flexWithItemsPreservesText(@TempDir Path tempDir) {
        Path output = tempDir.resolve("flex-items.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var flex = Flex.of()
                .direction(FlexDirection.ROW)
                .justifyContent(JustifyContent.SPACE_BETWEEN)
                .alignItems(AlignItems.CENTER);
            flex.addItem(Paragraph.of("Growing item")).grow(1);
            flex.addItem(Paragraph.of("Fixed item")).basis(100);
            doc.add(flex);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Growing item"), "Should contain grow item text");
            assertTrue(text.contains("Fixed item"), "Should contain fixed item text");
        }
    }

    @Test
    void flexColumnLayout(@TempDir Path tempDir) {
        Path output = tempDir.resolve("flex-col.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var flex = Flex.of()
                .direction(FlexDirection.COLUMN)
                .gap(5)
                .background(Color.LIGHT_GRAY)
                .padding(10, 20, 10, 20);
            flex.add(Paragraph.of("Top element"));
            flex.add(Paragraph.of("Bottom element"));
            doc.add(flex);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Top element"));
            assertTrue(text.contains("Bottom element"));
        }
    }
}
