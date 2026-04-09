package dev.foliopdf;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfRedactorTest {

    private byte[] buildSourcePdf(String body) {
        try (var doc = Document.builder().pageSize(PageSize.A4).margins(72).build()) {
            doc.add(Paragraph.of(body));
            return doc.toBytes();
        }
    }

    @Test
    void redactTextRemovesSensitiveString() {
        byte[] source = buildSourcePdf("Public info. SSN: 123-45-6789. More public info.");
        byte[] redacted;
        try (var reader = PdfReader.parse(source);
             var opts = new RedactOpts().fillColor(0, 0, 0).stripMetadata(true)) {
            redacted = PdfRedactor.text(reader, List.of("123-45-6789"), opts);
        }
        assertNotNull(redacted);
        assertTrue(redacted.length > 100);
        assertEquals("%PDF", new String(redacted, 0, 4));

        try (var out = PdfReader.parse(redacted)) {
            String text = out.extractText(0);
            assertFalse(text.contains("123-45-6789"),
                "redacted text should not contain SSN, got: " + text);
        }
    }

    @Test
    void redactPatternRemovesRegexMatches() {
        byte[] source = buildSourcePdf("Call 555-0100 or 555-0199 today.");
        byte[] redacted;
        try (var reader = PdfReader.parse(source);
             var opts = new RedactOpts()
                 .fillColor(0.1, 0.1, 0.1)
                 .overlay("X", 6, 1, 1, 1)) {
            redacted = PdfRedactor.pattern(reader, "555-\\d{4}", opts);
        }
        try (var out = PdfReader.parse(redacted)) {
            String text = out.extractText(0);
            assertFalse(text.contains("555-0100"));
            assertFalse(text.contains("555-0199"));
        }
    }

    @Test
    void redactRegionsProducesValidPdf() {
        byte[] source = buildSourcePdf("Top secret material below.");
        byte[] redacted;
        try (var reader = PdfReader.parse(source);
             var opts = new RedactOpts()) {
            redacted = PdfRedactor.regions(reader,
                List.of(new RedactRegion(0, 72, 72, 300, 120)),
                opts);
        }
        assertNotNull(redacted);
        assertEquals("%PDF", new String(redacted, 0, 4));
    }
}
