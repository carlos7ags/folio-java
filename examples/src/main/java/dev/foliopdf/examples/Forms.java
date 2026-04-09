// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Creates a PDF with interactive AcroForm fields: text inputs, checkboxes,
 * radio buttons, dropdowns, list boxes, and a signature field.
 *
 * <p>Open the generated PDF in Adobe Acrobat to interact with the fields.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Forms
 * </pre>
 */
public final class Forms {
    public static void main(String[] args) {
        var font = Font.helvetica();
        var bold = Font.helveticaBold();

        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title("Folio Forms Showcase")
                .author("Folio")
                .build()) {

            Page p = doc.addPage();

            // Title
            p.addText("Folio Forms Showcase", bold, 20, 72, 740);
            p.addText("Interactive AcroForm fields — open in Adobe Acrobat to edit.", font, 10, 72, 722);

            double y = 690;

            // Text Fields
            p.addText("Text Fields", bold, 13, 72, y);

            y -= 22;
            p.addText("Full Name:", font, 10, 72, y + 4);

            y -= 26;
            p.addText("Email:", font, 10, 72, y + 4);

            // Checkboxes
            y -= 36;
            p.addText("Checkboxes", bold, 13, 72, y);

            y -= 22;
            p.addText("Agree to terms:", font, 10, 72, y + 4);

            y -= 22;
            p.addText("Subscribe:", font, 10, 72, y + 4);

            // Dropdown
            y -= 36;
            p.addText("Dropdown", bold, 13, 72, y);

            y -= 22;
            p.addText("Country:", font, 10, 72, y + 4);

            // Build form
            try (var form = Form.of()) {
                form.addTextField("fullName", 180, 668, 350, 686, 0);
                form.addTextField("email", 180, 642, 350, 660, 0);
                form.addCheckbox("agreeTerms", 180, 618, 196, 634, 0, true);
                form.addCheckbox("subscribe", 180, 596, 196, 612, 0, false);
                form.addDropdown("country", 180, 546, 350, 566, 0,
                    "United States", "Germany", "Japan", "Brazil", "Australia");
                form.addSignature("signature", 180, 480, 400, 520, 0);
                doc.form(form);
            }

            doc.save("forms.pdf");
            System.out.println("Created forms.pdf — open in Adobe Acrobat to interact with fields");
        }
    }
}
