// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.io.File;

/**
 * Demonstrates OpenType shaping for Brahmic scripts. Devanagari is the
 * first implementation; Bengali, Tamil, Telugu, Kannada, Malayalam,
 * Gurmukhi, Odia, Assamese and Sinhala will slot into this example as
 * their shapers land. The layout pipeline routes Indic words through the
 * script-specific shaper automatically, so the example just loads a
 * capable font and adds paragraphs — reph, pre-base matra reordering,
 * half forms, conjuncts, nukta, below-base and post-base forms all
 * happen under the hood.
 *
 * <p>Required fonts (the Devanagari section is skipped if none resolve):
 *
 * <pre>
 * macOS   /System/Library/Fonts/Supplemental/Devanagari Sangam MN.ttc
 *         /System/Library/Fonts/Supplemental/ITFDevanagari.ttc
 *         /Library/Fonts/Arial Unicode.ttf
 * Linux   /usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf
 *         /usr/share/fonts/opentype/noto/NotoSansDevanagari-Regular.ttf
 * Windows C:\Windows\Fonts\mangal.ttf
 *         C:\Windows\Fonts\Nirmala.ttf
 * </pre>
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Indic
 * </pre>
 */
public final class Indic {

    public static void main(String[] args) {
        try (var doc = Document.builder()
                .letter()
                .title("Indic Text Shaping")
                .author("Folio")
                .build()) {

            doc.add(Heading.of("Indic Text Shaping", HeadingLevel.H1));
            doc.add(Paragraph.of(
                "Brahmic scripts need per-script shaping engines that reorder "
                + "codepoints, form conjuncts, and apply position-dependent "
                + "glyph substitutions. Folio runs the OpenType Indic shaping "
                + "pipeline on each word so the calling code does not.",
                Font.helvetica(), 11));

            devanagariSection(doc);

            doc.save("indic.pdf");
            System.out.println("Created indic.pdf");
        }
    }

    private static void devanagariSection(Document doc) {
        doc.add(Heading.of("Devanagari", HeadingLevel.H2));

        Font ef = loadDevanagariFont();
        if (ef == null) {
            doc.add(Paragraph.of(
                "No Devanagari font found on this system; section skipped. "
                + "Install a font such as Noto Sans Devanagari to enable it.",
                Font.helvetica(), 10));
            System.out.println("no Devanagari font found on this system; Devanagari section skipped");
            return;
        }

        try {
            doc.add(Paragraph.of(
                "Each paragraph below exercises a distinct phase of the OpenType "
                + "Indic shaping pipeline. Copy any paragraph out of the rendered "
                + "PDF and the original Unicode codepoints come back verbatim — "
                + "the shaper emits ActualText markers so copy/paste and "
                + "accessibility tools round-trip cleanly.",
                Font.helvetica(), 10));

            // "namaste duniya" — simple consonant + vowel sequence
            doc.add(Heading.of("Greeting", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded(
                "\u0928\u092E\u0938\u094D\u0924\u0947 \u0926\u0941\u0928\u093F\u092F\u093E", ef, 18));

            // "kshatriya" — kṣa conjunct, exercises the akhn (akhand) ligature
            doc.add(Heading.of("Conjunct (akhn)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded(
                "\u0915\u094D\u0937\u0924\u094D\u0930\u093F\u092F", ef, 18));

            // "kitna" — i-vowel sign U+093F is typed after ka but renders before
            doc.add(Heading.of("Pre-base matra reorder", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0915\u093F\u0924\u0928\u093E", ef, 18));

            // "karma" — ra + virama at start of a cluster becomes superscript reph
            doc.add(Heading.of("Reph (rphf)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0915\u0930\u094D\u092E", ef, 18));

            // "kaccha" — first ka in ka+halant+cha takes its half form
            doc.add(Heading.of("Half form (half)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0915\u091A\u094D\u091A\u093E", ef, 18));

            // "krama" — ra in ka+halant+ra drops below the base
            doc.add(Heading.of("Below-base form (blwf)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0915\u094D\u0930\u092E", ef, 18));

            // "qanun" — ka + nukta composes into the qa phoneme
            doc.add(Heading.of("Nukta (nukt)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0915\u093C\u093E\u0928\u0942\u0928", ef, 18));

            // "hindi" — ndi cluster exercises post-base consonant placement
            doc.add(Heading.of("Post-base form (pstf)", HeadingLevel.H3));
            doc.add(Paragraph.ofEmbedded("\u0939\u093F\u0928\u094D\u0926\u0940", ef, 18));
        } finally {
            ef.close();
        }
    }

    private static Font loadDevanagariFont() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String[] paths;
        if (os.contains("mac")) {
            paths = new String[]{
                "/System/Library/Fonts/Supplemental/Devanagari Sangam MN.ttc",
                "/System/Library/Fonts/Supplemental/ITFDevanagari.ttc",
                "/System/Library/Fonts/Supplemental/DevanagariMT.ttc",
                "/Library/Fonts/Arial Unicode.ttf",
            };
        } else if (os.contains("linux")) {
            paths = new String[]{
                "/usr/share/fonts/truetype/noto/NotoSansDevanagari-Regular.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansDevanagari-Regular.ttf",
                "/usr/share/fonts/noto/NotoSansDevanagari-Regular.ttf",
                "/usr/share/fonts/TTF/NotoSansDevanagari-Regular.ttf",
            };
        } else {
            paths = new String[]{
                "C:\\Windows\\Fonts\\mangal.ttf",
                "C:\\Windows\\Fonts\\Nirmala.ttf",
            };
        }
        for (String p : paths) {
            if (!new File(p).exists()) continue;
            try {
                return Font.loadTTF(p);
            } catch (RuntimeException ignored) {
                // try next
            }
        }
        return null;
    }
}
