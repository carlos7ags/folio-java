// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Imports an existing PDF page as a template and fills it with additional
 * content. Useful for filling blank forms or adding overlays to pre-designed
 * letterheads, certificates, coupons, etc.
 *
 * <p>This example creates a "template" PDF first (simulating a pre-designed
 * letterhead), then imports that page into a new document and overlays
 * personalization text on top.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.ImportPage
 * </pre>
 */
public final class ImportPage {

    public static void main(String[] args) throws Exception {
        // --- Step 1: Create a template PDF (simulated letterhead) ---
        System.out.println("Creating template PDF (simulated letterhead)...");
        byte[] templateBytes;
        try (var tpl = Document.builder()
                .letter()
                .title("Certificate Template")
                .margins(72)
                .build()) {

            Font helv = Font.helvetica();
            Font helvBold = Font.helveticaBold();

            tpl.add(Paragraph.of(" ", helv, 1).spaceBefore(80));
            tpl.add(Paragraph.of("CERTIFICATE OF COMPLETION", helvBold, 28)
                    .align(Align.CENTER));
            tpl.add(LineSeparator.of());
            tpl.add(Paragraph.of(" ", helv, 1).spaceBefore(20));
            tpl.add(Paragraph.of("This is to certify that", helv, 14)
                    .align(Align.CENTER));
            tpl.add(Paragraph.of(" ", helv, 1).spaceBefore(40));
            tpl.add(Paragraph.of("has successfully completed the course", helv, 14)
                    .align(Align.CENTER));
            tpl.add(Paragraph.of(" ", helv, 1).spaceBefore(80));
            tpl.add(LineSeparator.of());
            tpl.add(Paragraph.of("Folio Academy", helv, 11)
                    .align(Align.CENTER));

            Files.write(Path.of("certificate_template.pdf"), tpl.toBytes());
            templateBytes = tpl.toBytes();
            System.out.println("  Wrote certificate_template.pdf (" + templateBytes.length + " bytes)");
        }

        // --- Step 2: Import the template and overlay personalized content ---
        System.out.println("\nImporting template and overlaying personalization...");

        String[][] recipients = {
                {"Jane Doe", "Advanced PDF Generation"},
                {"John Smith", "PDF Accessibility and PDF/UA"},
                {"Priya Patel", "Digital Signatures and PKI"},
        };

        try (var reader = PdfReader.parse(templateBytes)) {

            for (String[] r : recipients) {
                String name = r[0];
                String course = r[1];
                String fileName = "certificate_" + name.toLowerCase().replace(' ', '_') + ".pdf";

                try (var doc = Document.builder()
                        .letter()
                        .title("Certificate — " + name)
                        .author("Folio Academy")
                        .margins(0)
                        .build()) {

                    // Add a blank page, then apply the template as a backdrop.
                    Page page = doc.addPage();
                    try (var tpl = PageImport.from(reader, 0)) {
                        tpl.applyTo(page);
                    }

                    // Overlay personalized text on top of the template.
                    // Coordinates are absolute (origin at bottom-left of page).
                    Font helvBoldItalic = Font.helveticaBoldOblique();
                    Font helv = Font.helvetica();

                    // Recipient name (centered around y=500)
                    page.addText(name, helvBoldItalic, 32, 0, 500);
                    // Course title (centered around y=400)
                    page.addText(course, helv, 18, 0, 400);

                    doc.save(fileName);
                    System.out.println("  Wrote " + fileName);
                }
            }
        }

        System.out.println("\nDone. Created " + recipients.length + " personalized certificates from one template.");
    }
}
