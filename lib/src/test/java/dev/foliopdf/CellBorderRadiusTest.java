package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellBorderRadiusTest {

    @Test
    void uniformAndPerCornerRadius() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(36).build()) {
            var table = Table.builder()
                .row(row -> {
                    row.addCell("Rounded").setBorderRadius(6);
                    row.addCell("Corners").setBorderRadius(2, 4, 8, 16);
                })
                .build();
            doc.add(table);
            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
