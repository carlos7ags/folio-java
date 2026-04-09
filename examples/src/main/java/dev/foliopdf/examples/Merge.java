// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Demonstrates reading, merging, and inspecting PDF documents:
 * creating PDFs in memory, parsing them with PdfReader, merging
 * with PdfMerger, and extracting text from the result.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Merge
 * </pre>
 */
public final class Merge {
    public static void main(String[] args) {
        // --- Create source PDFs in memory ---
        System.out.println("Creating source PDFs...");

        byte[] pdf1 = createReport("Q3 2026 Revenue Report", new String[] {
            "Total revenue reached $25.1M, up 18% year-over-year.",
            "Advisory services contributed $12.8M (51% of total).",
            "New client acquisitions increased by 23 accounts.",
        });

        byte[] pdf2 = createReport("Q4 2026 Revenue Report", new String[] {
            "Total revenue reached $28.3M, up 22% year-over-year.",
            "Operating margin improved to 30%, driven by cost optimization.",
            "Asia-Pacific region grew 31.7%, the fastest across all regions.",
        });

        byte[] pdf3 = createCoverPage("Annual Summary 2026", "Apex Capital Partners");

        // --- Inspect source PDFs ---
        try (var r1 = PdfReader.parse(pdf1);
             var r2 = PdfReader.parse(pdf2);
             var r3 = PdfReader.parse(pdf3)) {

            System.out.printf("  PDF 1: %d page(s), %d bytes%n", r1.pageCount(), pdf1.length);
            System.out.printf("  PDF 2: %d page(s), %d bytes%n", r2.pageCount(), pdf2.length);
            System.out.printf("  PDF 3: %d page(s), %d bytes%n", r3.pageCount(), pdf3.length);

            // --- Merge: cover + Q3 report + Q4 report ---
            System.out.println("\nMerging...");
            try (var merged = PdfMerger.merge(r3, r1, r2)) {
                merged.setInfo("Annual Summary 2026", "Apex Capital Partners");
                merged.save("merged.pdf");
            }
        }

        // --- Read back and inspect ---
        try (var reader = PdfReader.open("merged.pdf")) {
            System.out.printf("\nMerged PDF: %d pages%n", reader.pageCount());
            System.out.printf("  Title:  %s%n", reader.title());
            System.out.printf("  Author: %s%n", reader.author());

            System.out.println("\nExtracted text:");
            for (int i = 0; i < reader.pageCount(); i++) {
                String text = reader.extractText(i);
                String preview = text.length() > 80 ? text.substring(0, 80) + "..." : text;
                System.out.printf("  Page %d (%.0fx%.0f pt): %s%n",
                    i + 1, reader.pageWidth(i), reader.pageHeight(i), preview);
            }
        }

        System.out.println("\nCreated merged.pdf");
    }

    private static byte[] createReport(String title, String[] bullets) {
        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title(title)
                .author("Apex Capital Partners")
                .build()) {
            doc.add(Paragraph.of(title, Font.helveticaBold(), 18));
            doc.add(LineSeparator.of());
            for (String bullet : bullets) {
                doc.add(Paragraph.of("• " + bullet).leading(1.4).spaceAfter(4));
            }
            return doc.writeToBuffer();
        }
    }

    private static byte[] createCoverPage(String title, String subtitle) {
        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title(title)
                .build()) {
            var page = doc.addPage();
            page.addText(title, Font.helveticaBold(), 28, 72, 500);
            page.addText(subtitle, Font.helvetica(), 16, 72, 460);
            page.addText("Confidential", Font.helvetica(), 10, 72, 420);
            return doc.writeToBuffer();
        }
    }
}
