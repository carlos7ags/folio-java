// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.io.File;

/**
 * Demonstrates Right-To-Left text support: Hebrew bidi, Arabic contextual
 * shaping, the rlig lam-alef ligature, GPOS mark-to-base attachment for
 * harakat, kashida justification, and ActualText round-trip for
 * accessibility and copy/paste.
 *
 * <p>Required fonts (the example picks one per script — if neither script
 * has a font available that section is skipped):
 *
 * <pre>
 * Hebrew:
 *   macOS   /System/Library/Fonts/Supplemental/Arial.ttf
 *           /System/Library/Fonts/Supplemental/Tahoma.ttf
 *           /Library/Fonts/Arial Unicode.ttf
 *   Linux   /usr/share/fonts/truetype/dejavu/DejaVuSans.ttf
 *           /usr/share/fonts/truetype/noto/NotoSansHebrew-Regular.ttf
 *   Windows C:\Windows\Fonts\arial.ttf
 *           C:\Windows\Fonts\tahoma.ttf
 *
 * Arabic:
 *   macOS   /System/Library/Fonts/SFArabic.ttf
 *           /System/Library/Fonts/Supplemental/Arial.ttf
 *           /Library/Fonts/Arial Unicode.ttf
 *   Linux   /usr/share/fonts/truetype/noto/NotoSansArabic-Regular.ttf
 *           /usr/share/fonts/opentype/noto/NotoSansArabic-Regular.ttf
 *   Windows C:\Windows\Fonts\arial.ttf
 *           C:\Windows\Fonts\tahoma.ttf
 * </pre>
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Rtl
 * </pre>
 */
public final class Rtl {

    public static void main(String[] args) {
        try (var doc = Document.builder()
                .letter()
                .title("Right-To-Left Text")
                .author("Folio")
                .build()) {

            doc.add(Heading.of("Right-To-Left Text Support", HeadingLevel.H1));

            // Load Hebrew and Arabic fonts independently. On most systems no
            // single file covers both scripts well, so pick the best font per
            // script and use each in its own section.
            Font hebrew = loadHebrewFont();
            Font arabic = loadArabicFont();

            if (hebrew == null && arabic == null) {
                doc.add(Paragraph.of(
                    "(No Hebrew or Arabic font found on this system. "
                    + "Install Arial, Tahoma, or a Noto Sans variant to see "
                    + "rendered RTL text.)",
                    Font.helvetica(), 11));
            }

            // --- Section 1: Hebrew (bidi only, no shaping needed) ---

            doc.add(Heading.of("1. Hebrew", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Hebrew text uses the Unicode Bidirectional Algorithm for correct "
                + "word ordering. The paragraph auto-detects RTL from the first "
                + "strong character and defaults to right-alignment.",
                Font.helvetica(), 11));

            if (hebrew != null) {
                doc.add(Paragraph.ofEmbedded(
                    "\u05E9\u05DC\u05D5\u05DD \u05E2\u05D5\u05DC\u05DD",  // שלום עולם
                    hebrew, 14));
                doc.add(Paragraph.ofEmbedded(
                    "Folio \u05EA\u05D5\u05DE\u05DA \u05D1\u05E2\u05D1\u05E8\u05D9\u05EA"
                    + " and \u05E2\u05E8\u05D1\u05D9\u05EA",
                    hebrew, 12));
            }

            // --- Section 2: Arabic (bidi + contextual shaping) ---

            doc.add(Heading.of("2. Arabic Contextual Shaping", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Arabic letters have four positional forms (isolated, initial, "
                + "medial, final) that are selected based on each letter's neighbors. "
                + "Folio applies Presentation Forms-B substitution automatically.",
                Font.helvetica(), 11));

            if (arabic != null) {
                // "bismillah" = بسم الله
                doc.add(Paragraph.ofEmbedded(
                    "\u0628\u0633\u0645 \u0627\u0644\u0644\u0647", arabic, 16));
                // Farsi: "salam donya" = سلام دنیا
                doc.add(Paragraph.ofEmbedded(
                    "\u0633\u0644\u0627\u0645 \u062F\u0646\u06CC\u0627", arabic, 16));
            }

            // --- Section 2a: Arabic ligatures (rlig) ---

            doc.add(Heading.of("2a. Arabic Ligatures (rlig)", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "The lam-alef ligature is a required ligature in Arabic: whenever "
                + "lam is followed by alef, the pair composes into a single glyph. "
                + "Folio applies the rlig GSUB feature automatically after "
                + "positional shaping.",
                Font.helvetica(), 11));

            if (arabic != null) {
                // "la ilaha" = لا إله — exercises lam-alef ligature
                doc.add(Paragraph.ofEmbedded(
                    "\u0644\u0627 \u0625\u0644\u0647", arabic, 20));
            }

            // --- Section 2b: Arabic with harakat (GPOS marks) ---

            doc.add(Heading.of("2b. Arabic with Harakat (GPOS marks)", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Vowel marks (harakat) are positioned on each base letter's anchor "
                + "via the OpenType GPOS mark-to-base feature. The combining marks "
                + "contribute zero advance and sit at the correct x/y offset "
                + "recorded in the font's mark anchor table.",
                Font.helvetica(), 11));

            if (arabic != null) {
                // Vocalized bismillah: بِسْمِ اللَّهِ
                doc.add(Paragraph.ofEmbedded(
                    "\u0628\u0650\u0633\u0652\u0645\u0650 \u0627\u0644\u0644\u0651\u064E\u0647\u0650",
                    arabic, 20));
            }

            // --- Section 2c: Kashida justification ---

            doc.add(Heading.of("2c. Kashida Justification", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "When Arabic text is justified, Folio inserts tatweel (U+0640, "
                + "also called kashida) between dual-joining letters to elongate "
                + "the connector instead of stretching whitespace. Watch the "
                + "connectors lengthen in the paragraph below.",
                Font.helvetica(), 11));

            if (arabic != null) {
                // "The people want justice, freedom, and equality for all citizens."
                doc.add(Paragraph.ofEmbedded(
                    "\u0627\u0644\u0634\u0639\u0628 \u064A\u0631\u064A\u062F "
                    + "\u0627\u0644\u0639\u062F\u0627\u0644\u0629 \u0648\u0627\u0644\u062D\u0631\u064A\u0629 "
                    + "\u0648\u0627\u0644\u0645\u0633\u0627\u0648\u0627\u0629 \u0644\u062C\u0645\u064A\u0639 "
                    + "\u0627\u0644\u0645\u0648\u0627\u0637\u0646\u064A\u0646",
                    arabic, 18).align(Align.JUSTIFY));
            }

            // --- Section 2d: ActualText round-trip ---

            doc.add(Heading.of("2d. ActualText Round-Trip", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Every shaped Arabic word above is wrapped in an ISO 32000-2 "
                + "ActualText marker that carries the original Unicode. Copying "
                + "text out of this PDF or running pdftotext on it returns the "
                + "original codepoints, not the Presentation Forms-B glyph "
                + "substitutions that the shaper emitted.",
                Font.helvetica(), 11));

            // --- Section 3: Mixed bidi with numbers ---

            doc.add(Heading.of("3. Mixed Bidirectional Text", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Numbers in RTL text stay left-to-right per the Unicode bidi "
                + "algorithm. Brackets are mirrored in RTL runs: ( becomes ) "
                + "and vice versa.",
                Font.helvetica(), 11));

            if (hebrew != null) {
                // "Section 42 of the law"
                doc.add(Paragraph.ofEmbedded(
                    "\u05E1\u05E2\u05D9\u05E3 42 \u05D1\u05D7\u05D5\u05E7", hebrew, 14));
                // Brackets in RTL: "(שלום)"
                doc.add(Paragraph.ofEmbedded(
                    "(\u05E9\u05DC\u05D5\u05DD)", hebrew, 14));
            }

            // --- Section 4: Explicit direction control ---

            doc.add(Heading.of("4. Explicit Direction Control", HeadingLevel.H2));
            doc.add(Paragraph.of(
                "Paragraph.setDirection(Direction.RTL) forces RTL alignment "
                + "even for text with no strong directional characters. "
                + "align(Align.LEFT) overrides the RTL default right-alignment.",
                Font.helvetica(), 11));

            // Punctuation-only with RTL direction → right-aligned
            doc.add(Paragraph.of("......", Font.helvetica(), 12)
                .setDirection(Direction.RTL));

            // RTL paragraph with explicit left override
            if (hebrew != null) {
                doc.add(Paragraph.ofEmbedded(
                    "\u05E9\u05DC\u05D5\u05DD \u05E2\u05D5\u05DC\u05DD", hebrew, 14)
                    .align(Align.LEFT));
            }

            doc.save("rtl.pdf");
            System.out.println("Created rtl.pdf");

            if (hebrew != null) hebrew.close();
            if (arabic != null) arabic.close();
        }
    }

    private static Font loadHebrewFont() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] paths;
        if (os.contains("mac")) {
            paths = new String[]{
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Tahoma.ttf",
                "/Library/Fonts/Arial Unicode.ttf",
            };
        } else if (os.contains("linux")) {
            paths = new String[]{
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/noto/NotoSansHebrew-Regular.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansHebrew-Regular.ttf",
            };
        } else {
            paths = new String[]{
                "C:\\Windows\\Fonts\\arial.ttf",
                "C:\\Windows\\Fonts\\tahoma.ttf",
            };
        }
        return loadFirst(paths);
    }

    private static Font loadArabicFont() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] paths;
        if (os.contains("mac")) {
            paths = new String[]{
                "/System/Library/Fonts/SFArabic.ttf",
                "/System/Library/Fonts/Supplemental/Arial.ttf",
                "/System/Library/Fonts/Supplemental/Tahoma.ttf",
                "/Library/Fonts/Arial Unicode.ttf",
            };
        } else if (os.contains("linux")) {
            paths = new String[]{
                "/usr/share/fonts/truetype/noto/NotoSansArabic-Regular.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansArabic-Regular.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            };
        } else {
            paths = new String[]{
                "C:\\Windows\\Fonts\\arial.ttf",
                "C:\\Windows\\Fonts\\tahoma.ttf",
            };
        }
        return loadFirst(paths);
    }

    private static Font loadFirst(String[] paths) {
        for (String p : paths) {
            if (!new File(p).exists()) continue;
            try {
                return Font.loadTTF(p);
            } catch (RuntimeException ignored) {
                // fall through to next candidate
            }
        }
        return null;
    }
}
