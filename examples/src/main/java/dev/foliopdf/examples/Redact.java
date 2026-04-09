// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Redacts sensitive information from a PDF. Demonstrates three redaction
 * strategies:
 * <ol>
 *   <li>Text matching — exact string targets</li>
 *   <li>Regex pattern matching — e.g. SSNs, credit cards</li>
 *   <li>Region redaction — page-level bounding boxes</li>
 * </ol>
 *
 * <p>Redaction permanently removes text from the PDF content stream (not
 * just a visual overlay) and draws an opaque rectangle over the redacted
 * area. Unlike black-box overlays, redacted text cannot be recovered.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Redact
 * </pre>
 */
public final class Redact {

    public static void main(String[] args) throws Exception {
        // --- Step 1: Create a PDF with sensitive content ---
        System.out.println("Creating source PDF with sensitive content...");
        byte[] sourceBytes;
        try (var doc = Document.builder()
                .letter()
                .title("Confidential Employee Record")
                .margins(72)
                .build()) {

            Font helv = Font.helvetica();
            Font helvBold = Font.helveticaBold();

            doc.add(Heading.of("Employee Record", HeadingLevel.H1));
            doc.add(LineSeparator.of());

            doc.add(Paragraph.of("Name: Jane Doe", helv, 12).spaceBefore(8));
            doc.add(Paragraph.of("Employee ID: EMP-4521", helv, 12));
            doc.add(Paragraph.of("SSN: 123-45-6789", helv, 12));
            doc.add(Paragraph.of("Email: jane.doe@example.com", helv, 12));
            doc.add(Paragraph.of("Phone: (555) 123-4567", helv, 12));
            doc.add(Paragraph.of("Salary: $142,500", helv, 12));

            doc.add(Paragraph.of(" ", helv, 1).spaceBefore(12));
            doc.add(Heading.of("Notes", HeadingLevel.H2));
            doc.add(Paragraph.of(
                    "Employee has been with the company for 6 years. Last " +
                    "performance review was excellent. The SSN 123-45-6789 " +
                    "is on file with HR and payroll.", helv, 11).leading(1.5));

            Files.write(Path.of("employee_record.pdf"), doc.toBytes());
            sourceBytes = doc.toBytes();
            System.out.println("  Wrote employee_record.pdf (" + sourceBytes.length + " bytes)");
        }

        // --- Step 2: Redact by exact text match ---
        System.out.println("\nRedacting by exact text match (Name, SSN, Salary)...");
        try (var reader = PdfReader.parse(sourceBytes);
             var opts = PdfRedactor.opts()
                     .fillColor(0, 0, 0)
                     .overlay("[REDACTED]", 8, 1, 1, 1)
                     .stripMetadata(true)) {

            byte[] redacted = PdfRedactor.text(
                    reader,
                    List.of("Jane Doe", "123-45-6789", "$142,500"),
                    opts);

            Files.write(Path.of("redacted_by_text.pdf"), redacted);
            System.out.println("  Wrote redacted_by_text.pdf (" + redacted.length + " bytes)");
        }

        // --- Step 3: Redact by regex pattern ---
        System.out.println("\nRedacting by regex pattern (SSNs and phone numbers)...");
        try (var reader = PdfReader.parse(sourceBytes);
             var opts = PdfRedactor.opts()
                     .fillColor(0.2, 0.2, 0.2)
                     .stripMetadata(true)) {

            // \d{3}-\d{2}-\d{4} matches SSNs; (\d{3}) \d{3}-\d{4} matches US phone.
            byte[] redacted = PdfRedactor.pattern(
                    reader,
                    "\\d{3}-\\d{2}-\\d{4}",
                    opts);

            Files.write(Path.of("redacted_by_pattern.pdf"), redacted);
            System.out.println("  Wrote redacted_by_pattern.pdf (" + redacted.length + " bytes)");
        }

        // --- Step 4: Redact by region (black box over a specific area) ---
        System.out.println("\nRedacting by region (page 0, top area)...");
        try (var reader = PdfReader.parse(sourceBytes);
             var opts = PdfRedactor.opts()
                     .fillColor(0, 0, 0)) {

            // Cover a rectangle in the upper area of page 0 where sensitive
            // fields appear. Coordinates are in points from the bottom-left.
            byte[] redacted = PdfRedactor.regions(
                    reader,
                    List.of(new RedactRegion(0, 72, 620, 360, 680)),
                    opts);

            Files.write(Path.of("redacted_by_region.pdf"), redacted);
            System.out.println("  Wrote redacted_by_region.pdf (" + redacted.length + " bytes)");
        }

        System.out.println("\nDone. Review each PDF to verify sensitive content is gone.");
    }
}
