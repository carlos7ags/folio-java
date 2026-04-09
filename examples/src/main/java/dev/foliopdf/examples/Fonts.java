// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

/**
 * Demonstrates the 14 standard PDF fonts and custom embedded fonts via the
 * HTML converter. On macOS, discovers system fonts from /System/Library/Fonts.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Fonts
 * </pre>
 */
public final class Fonts {
    public static void main(String[] args) {
        String os = System.getProperty("os.name", "").toLowerCase();

        // Build @font-face CSS for discovered system fonts
        String[][] systemFonts;
        if (os.contains("mac")) {
            systemFonts = new String[][] {
                {"Verdana", "/System/Library/Fonts/Supplemental/Verdana.ttf"},
                {"Georgia", "/System/Library/Fonts/Supplemental/Georgia.ttf"},
                {"Impact", "/System/Library/Fonts/Supplemental/Impact.ttf"},
            };
        } else if (os.contains("linux")) {
            systemFonts = new String[][] {
                {"DejaVuSans", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"},
                {"NotoSans", "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"},
            };
        } else {
            systemFonts = new String[][] {
                {"Arial", "C:\\Windows\\Fonts\\arial.ttf"},
                {"Verdana", "C:\\Windows\\Fonts\\verdana.ttf"},
            };
        }

        var css = new StringBuilder();
        var body = new StringBuilder();
        for (String[] f : systemFonts) {
            if (new java.io.File(f[1]).exists()) {
                css.append("@font-face { font-family: '").append(f[0])
                   .append("'; src: url('").append(f[1]).append("'); }\n");
                body.append("<hr/><h2 style=\"font-family: '").append(f[0]).append("'\">")
                    .append(f[0]).append(" (custom @font-face)</h2>\n")
                    .append("<p style=\"font-family: '").append(f[0])
                    .append("'\">The quick brown fox jumps over the lazy dog. 0123456789</p>\n");
            }
        }

        String html = """
            <html><head><style>
            %s
            body { margin: 30px; }
            h1 { font-size: 22px; color: #1a1a2e; margin-bottom: 8px; }
            h2 { font-size: 14px; color: #16213e; margin-top: 14px; margin-bottom: 4px; }
            p { margin-bottom: 6px; font-size: 12px; }
            .helvetica { font-family: Helvetica; }
            .times { font-family: "Times New Roman", serif; }
            .courier { font-family: "Courier New", monospace; }
            hr { margin: 12px 0; }
            </style></head><body>
            <h1 class="helvetica">Folio Font Showcase</h1>
            <p class="helvetica">This PDF demonstrates standard and custom fonts rendered by the Folio Java SDK.</p>
            <hr/>
            <h2 class="helvetica">Standard PDF Fonts</h2>
            <p class="helvetica"><b>Helvetica:</b> The quick brown fox jumps over the lazy dog. 0123456789</p>
            <p class="times"><b>Times:</b> The quick brown fox jumps over the lazy dog. 0123456789</p>
            <p class="courier"><b>Courier:</b> The quick brown fox jumps over the lazy dog. 0123456789</p>
            %s
            </body></html>
            """.formatted(css.toString(), body.toString());

        HtmlConverter.toPdf(html, "fonts.pdf");
        System.out.println("Created fonts.pdf");
    }
}
