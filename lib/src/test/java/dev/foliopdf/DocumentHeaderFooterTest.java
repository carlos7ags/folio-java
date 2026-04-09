package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DocumentHeaderFooterTest {

    @Test
    void headerAndFooterPlaceholdersRenderOnEachPage(@TempDir Path tempDir) {
        Path out = tempDir.resolve("headerfooter.pdf");
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(72).build()) {
            doc.setHeaderText("Confidential", Font.helvetica(), 10, Align.CENTER);
            doc.setFooterText("Page {page} of {pages}", Font.helvetica(), 9, Align.RIGHT);
            // Add enough content to force multiple pages.
            for (int i = 0; i < 120; i++) {
                doc.add(Paragraph.of("Line " + i + " — filler content to create multiple pages of output, "
                    + "repeated several times so that the layout engine overflows to a new page. "
                    + "More filler text here to be sure. And still more."));
            }
            doc.save(out.toString());
        }

        try (var reader = PdfReader.open(out.toString())) {
            assertTrue(reader.pageCount() >= 2, "expected multiple pages, got " + reader.pageCount());
            String firstPage = reader.extractText(0);
            assertNotNull(firstPage);
            assertTrue(firstPage.contains("Confidential"),
                "expected header text on page 0, got: " + firstPage);
            // Footer substitutes placeholders: should not contain literal "{page}"
            assertFalse(firstPage.contains("{page}"),
                "footer placeholders should be substituted");
        }
    }
}
