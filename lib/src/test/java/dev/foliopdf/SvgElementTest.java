package dev.foliopdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SvgElementTest {

    @Test
    void parseSvgDimensions() {
        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="200" height="150">
                <rect width="200" height="150" fill="red"/>
            </svg>
            """;

        try (var svgElem = SvgElement.parse(svg)) {
            assertEquals(200, svgElem.svgWidth(), 1, "SVG width should match");
            assertEquals(150, svgElem.svgHeight(), 1, "SVG height should match");
        }
    }

    @Test
    void svgInDocumentProducesLargerPdf(@TempDir Path tempDir) {
        Path withSvg = tempDir.resolve("svg.pdf");
        Path without = tempDir.resolve("empty.pdf");

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build()) {
            doc.add(Paragraph.of("No SVG."));
            doc.save(without.toString());
        }

        String svg = """
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
                <circle cx="50" cy="50" r="40" fill="blue"/>
            </svg>
            """;

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var svgElem = SvgElement.parse(svg)) {
            svgElem.size(200, 200).align(Align.CENTER);
            doc.add(Paragraph.of("SVG below:"));
            doc.add(svgElem);
            doc.save(withSvg.toString());
        }

        assertDoesNotThrow(() -> {
            long sizeWith = Files.size(withSvg);
            long sizeWithout = Files.size(without);
            assertTrue(sizeWith > sizeWithout,
                "PDF with SVG (%d) should be larger than without (%d)".formatted(sizeWith, sizeWithout));
        });

        try (var reader = PdfReader.open(withSvg.toString())) {
            assertEquals(1, reader.pageCount());
            assertTrue(reader.extractText(0).contains("SVG below"),
                "Should preserve surrounding text");
        }
    }

    @Test
    void svgFromBytes(@TempDir Path tempDir) {
        Path output = tempDir.resolve("svg-bytes.pdf");
        byte[] svgBytes = """
            <svg xmlns="http://www.w3.org/2000/svg" width="50" height="50">
                <rect width="50" height="50" fill="green"/>
            </svg>
            """.getBytes();

        try (var doc = Document.builder().pageSize(PageSize.LETTER).build();
             var svgElem = SvgElement.parseBytes(svgBytes)) {
            doc.add(svgElem);
            doc.save(output.toString());
        }

        try (var reader = PdfReader.open(output.toString())) {
            assertEquals(1, reader.pageCount());
        }
    }
}
