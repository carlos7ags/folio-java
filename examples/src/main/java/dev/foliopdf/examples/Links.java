// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Demonstrates link features: external hyperlinks, internal navigation,
 * styled links, and PDF bookmarks/outlines.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Links
 * </pre>
 */
public final class Links {
    public static void main(String[] args) {
        try (var doc = Document.builder()
                .pageSize(PageSize.LETTER)
                .title("Folio Links Showcase")
                .author("Folio")
                .build()) {

            // --- Page 1: HTML-generated links ---
            doc.addHtml("""
                <html><head><style>
                body { font-family: Helvetica; font-size: 10px; margin: 0; }
                h1 { font-size: 18px; color: #1a1a2e; margin-bottom: 3px; }
                h2 { font-size: 11px; color: #2c3e50; margin-top: 8px; margin-bottom: 2px; }
                p { margin-bottom: 3px; line-height: 1.3; }
                a { color: #2563eb; }
                hr { margin: 6px 0; }
                </style></head><body>
                <h1>Folio Links Showcase</h1>
                <p>This PDF demonstrates link features supported by the Folio Java SDK.</p>
                <hr/>
                <h2>External Hyperlinks</h2>
                <p><a href="https://github.com/carlos7ags/folio">Folio on GitHub</a></p>
                <p><a href="https://www.example.com">Example.com</a></p>
                <h2>Inline Links</h2>
                <p>Visit the <a href="https://dev.java">Java developer portal</a> for tutorials.</p>
                <h2>Styled Links</h2>
                <p><a href="https://example.com/red" style="color: #dc2626;">Red link</a> —
                   <a href="https://example.com/green" style="color: #059669;">Green link</a> —
                   <a href="https://example.com/purple" style="color: #7c3aed;">Purple link</a></p>
                </body></html>
                """);

            // --- Page 2: Layout API links ---
            doc.add(AreaBreak.of());
            doc.add(Heading.of("Layout API Links", HeadingLevel.H1));
            doc.add(Paragraph.of("These links use the Java SDK layout API directly."));

            doc.add(Link.of("Folio on GitHub", "https://github.com/carlos7ags/folio",
                    Font.helvetica(), 11)
                .color(Color.BLUE).underline());

            doc.add(Link.of("Java SE Documentation", "https://docs.oracle.com/en/java/",
                    Font.timesRoman(), 11)
                .color(Color.hex("#059669")).underline());

            // Internal link
            doc.add(Paragraph.of("Click below to jump back to page 1:"));
            doc.add(Link.internal("Go to page 1", "page1",
                    Font.helveticaBold(), 11)
                .color(Color.hex("#7c3aed")).underline());

            // Named destinations + outlines
            doc.addNamedDest("page1", 0, "Fit", 0, 0, 0);
            doc.addNamedDest("page2", 1, "Fit", 0, 0, 0);

            long sec1 = doc.addOutline("HTML Links", 0);
            Document.outlineAddChild(sec1, "External Hyperlinks", 0);
            Document.outlineAddChild(sec1, "Styled Links", 0);
            doc.addOutline("Layout API Links", 1);

            doc.save("links.pdf");
            System.out.println("Created links.pdf");
        }
    }
}
