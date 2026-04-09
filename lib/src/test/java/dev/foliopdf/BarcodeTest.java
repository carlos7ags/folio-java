package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BarcodeTest {

    @Test
    void qrBarcodeProducesLargerPdf(@TempDir Path tempDir) {
        Path withBarcode = tempDir.resolve("qr.pdf");
        Path without = tempDir.resolve("empty.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("Empty doc."));
            doc.save(without.toString());
        }

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var bc = Barcode.qr("https://example.com", 150)) {
            bc.align(Align.CENTER);
            doc.add(Paragraph.of("QR code below:"));
            doc.add(bc);
            doc.save(withBarcode.toString());
        }

        assertDoesNotThrow(() -> {
            long sizeWith = Files.size(withBarcode);
            long sizeWithout = Files.size(without);
            assertTrue(sizeWith > sizeWithout,
                "PDF with barcode (%d) should be larger than empty (%d)".formatted(sizeWith, sizeWithout));
        });

        try (var reader = PdfReader.open(withBarcode.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("QR code below"),
                "Should preserve surrounding text");
        }
    }

    @Test
    void code128BarcodeInDocument(@TempDir Path tempDir) {
        Path output = tempDir.resolve("code128.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var bc = Barcode.code128("ABC-123-XYZ", 200)) {
            bc.height(50);
            assertTrue(bc.barcodeWidth() > 0, "Barcode should have positive width");
            assertTrue(bc.barcodeHeight() > 0, "Barcode should have positive height");
            doc.add(Paragraph.of("Barcode label: ABC-123-XYZ"));
            doc.add(bc);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertTrue(reader.extractText(0).contains("ABC-123-XYZ"),
                "Should contain barcode label text");
        }
    }

    @Test
    void qrWithEccLevel(@TempDir Path tempDir) {
        Path output = tempDir.resolve("qr-ecc.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var bc = Barcode.qr("High ECC data", 100, ECCLevel.H)) {
            doc.add(bc);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
        }
    }
}
