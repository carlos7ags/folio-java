package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GridTemplateAreasTest {

    @Test
    void templateAreasAndBordersProduceValidPdf() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(36).build()) {
            var grid = Grid.of()
                .templateColumns(
                    new GridTrackType[]{GridTrackType.FR, GridTrackType.FR},
                    new double[]{1, 2})
                .setTemplateAreas("header header", "nav main", "footer footer")
                .setBorder(1.0, 0.1, 0.1, 0.1)
                .setBorders(
                    2, 1, 0, 0,
                    1, 0, 1, 0,
                    1, 0, 0, 1,
                    1, 0.5, 0.5, 0.5);
            grid.addChild(Paragraph.of("A"));
            grid.addChild(Paragraph.of("B"));
            grid.addChild(Paragraph.of("C"));
            doc.add(grid);
            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
