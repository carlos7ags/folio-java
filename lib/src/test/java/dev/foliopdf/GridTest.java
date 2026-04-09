package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GridTest {

    @Test
    void gridPreservesChildText(@TempDir Path tempDir) {
        Path output = tempDir.resolve("grid.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var grid = Grid.of()
                .templateColumns(
                    new GridTrackType[]{GridTrackType.FR, GridTrackType.FR},
                    new double[]{1, 1})
                .gap(10, 10)
                .padding(20);
            grid.addChild(Paragraph.of("Grid Cell A"));
            grid.addChild(Paragraph.of("Grid Cell B"));
            grid.addChild(Paragraph.of("Grid Cell C"));
            grid.addChild(Paragraph.of("Grid Cell D"));
            doc.add(grid);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Grid Cell A"), "Should contain cell A");
            assertTrue(text.contains("Grid Cell B"), "Should contain cell B");
            assertTrue(text.contains("Grid Cell C"), "Should contain cell C");
            assertTrue(text.contains("Grid Cell D"), "Should contain cell D");
        }
    }

    @Test
    void gridWithPlacement(@TempDir Path tempDir) {
        Path output = tempDir.resolve("grid-placement.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            var grid = Grid.of()
                .templateColumns(
                    new GridTrackType[]{GridTrackType.FR, GridTrackType.FR, GridTrackType.FR},
                    new double[]{1, 1, 1})
                .gap(5, 5);
            grid.addChild(Paragraph.of("Spanning cell"));
            grid.addChild(Paragraph.of("Normal cell"));
            grid.placement(0, 0, 2, 0, 1);
            doc.add(grid);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            String text = reader.extractText(0);
            assertTrue(text.contains("Spanning cell"));
            assertTrue(text.contains("Normal cell"));
        }
    }
}
