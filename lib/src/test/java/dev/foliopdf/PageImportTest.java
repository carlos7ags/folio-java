package dev.foliopdf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageImportTest {

    @Test
    void importAndApplyTemplatePage() {
        // Build a simple source PDF to import as a template.
        byte[] template;
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(36).build()) {
            doc.add(Paragraph.of("TEMPLATE WATERMARK"));
            template = doc.toBytes();
        }

        byte[] out;
        try (var reader = PdfReader.parse(template);
             var imp = PageImport.from(reader, 0)) {
            assertTrue(imp.width() > 0);
            assertTrue(imp.height() > 0);

            try (var doc = Document.builder().pageSize(PageSize.A4).margins(36).build()) {
                Page p = doc.addPage();
                imp.applyTo(p);
                p.addText("Overlaid content", Font.helvetica(), 12, 72, 700);
                out = doc.toBytes();
            }
        }

        assertNotNull(out);
        assertEquals("%PDF", new String(out, 0, 4));
    }
}
