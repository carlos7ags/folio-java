package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunListHighlightTest {

    @Test
    void lastSetBackgroundColorProducesValidPdf() {
        byte[] bytes;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(72).build();
             var runs = RunList.of()) {
            runs.add("normal ", Font.helvetica(), 12, Color.BLACK)
                .add("HIGHLIGHTED", Font.helvetica(), 12, Color.BLACK)
                .lastSetBackgroundColor(1.0, 1.0, 0.0)
                .add(" normal", Font.helvetica(), 12, Color.BLACK);

            var heading = Heading.of("Title", HeadingLevel.H1);
            heading.setRuns(runs);
            doc.add(heading);
            bytes = doc.toBytes();
        }
        assertTrue(bytes.length > 100);
        assertEquals("%PDF", new String(bytes, 0, 4));
    }
}
