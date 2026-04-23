// Copyright 2026 Carlos Munoz and the Folio Authors
// SPDX-License-Identifier: Apache-2.0
package dev.foliopdf.examples;

import dev.foliopdf.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

/**
 * Compares the default writer with the optimized writer (cross-reference
 * streams per ISO 32000-1 §7.5.8 plus object streams per §7.5.7, orphan
 * sweep over §7.5.4 reachability, and Flate recompression of eligible
 * payloads per §7.4.4) across several document shapes and reports the
 * byte-size delta for each.
 *
 * <p>The compression ratio depends heavily on what the document contains:
 * content streams produced by Folio's writer are already at high
 * compression and cannot benefit from re-deflate, so layout-built
 * documents see most of their gains from object-stream packing and the
 * orphan sweep. Imported documents (built by parsing a source PDF and
 * copying pages into a new Document) carry their content streams in raw
 * plaintext form — that is where recompression wins big.
 *
 * <pre>
 * ./gradlew examples:run -PmainClass=dev.foliopdf.examples.Optimize
 * </pre>
 */
public final class Optimize {

    record Fixture(String name, Supplier<Document> build) {}

    public static void main(String[] args) throws Exception {
        List<Fixture> fixtures = List.of(
            new Fixture("text-heavy", Optimize::textHeavy),
            new Fixture("many empty pages", Optimize::manyPages),
            new Fixture("table-heavy", Optimize::tableHeavy),
            new Fixture("imported text-heavy", Optimize::importedTextHeavy)
        );

        System.out.printf("%-22s %12s %12s %12s %12s %12s %10s%n",
            "fixture", "default", "xref+obj", "+sweep", "+recompress", "+full", "saved");
        System.out.println("---------------------- ------------ ------------ ------------ ------------ ------------ ----------");

        for (Fixture f : fixtures) {
            byte[] def         = writeDefault(f);
            byte[] packed      = writePacked(f);
            byte[] swept       = writeSwept(f);
            byte[] recompress  = writeRecompress(f);
            byte[] full        = writeFull(f);
            int saved = def.length - full.length;
            double pct = 100.0 * saved / def.length;
            System.out.printf("%-22s %10d B %10d B %10d B %10d B %10d B %8.1f %%%n",
                f.name(), def.length, packed.length, swept.length, recompress.length, full.length, pct);
        }

        // Write the imported fixture to disk so the user has concrete files
        // to inspect with qpdf or any PDF viewer. The imported case is
        // where the optimizer's win is most visible.
        Fixture last = fixtures.get(fixtures.size() - 1);
        byte[] def = writeDefault(last);
        byte[] full = writeFull(last);
        Files.write(Path.of("optimize-default.pdf"), def);
        Files.write(Path.of("optimize-compressed.pdf"), full);
        System.out.println();
        System.out.println("wrote optimize-default.pdf and optimize-compressed.pdf (imported text-heavy fixture)");
    }

    private static byte[] writeDefault(Fixture f) {
        try (var doc = f.build().get()) {
            return doc.toBytes();
        }
    }

    private static byte[] writePacked(Fixture f) {
        try (var doc = f.build().get();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .build()) {
            return doc.toBytesWithOptions(opts);
        }
    }

    private static byte[] writeSwept(Fixture f) {
        try (var doc = f.build().get();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .orphanSweep(true)
                 .build()) {
            return doc.toBytesWithOptions(opts);
        }
    }

    private static byte[] writeRecompress(Fixture f) {
        try (var doc = f.build().get();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .orphanSweep(true)
                 .recompressStreams(true)
                 .build()) {
            return doc.toBytesWithOptions(opts);
        }
    }

    private static byte[] writeFull(Fixture f) {
        try (var doc = f.build().get();
             var opts = WriteOptions.builder()
                 .useXrefStream(true)
                 .useObjectStreams(true)
                 .orphanSweep(true)
                 .cleanContentStreams(true)
                 .deduplicateObjects(true)
                 .recompressStreams(true)
                 .build()) {
            return doc.toBytesWithOptions(opts);
        }
    }

    private static Document textHeavy() {
        var doc = Document.builder()
            .letter()
            .title("Text-heavy fixture")
            .build();
        Font helv = Font.helvetica();
        for (int i = 1; i <= 25; i++) {
            doc.add(Heading.of("Section " + i, HeadingLevel.H1));
            doc.add(Paragraph.of(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do "
                + "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut "
                + "enim ad minim veniam, quis nostrud exercitation ullamco laboris.",
                helv, 11));
        }
        return doc;
    }

    private static Document manyPages() {
        // Page-tree-heavy: many empty pages produce many small dictionaries
        // (one page object plus its resources per page) and almost no
        // content stream bytes. This is the shape where the optimizer wins
        // the most because nearly every object is eligible for packing.
        var doc = Document.builder()
            .letter()
            .title("Many empty pages fixture")
            .build();
        for (int i = 0; i < 50; i++) {
            doc.addPage();
        }
        return doc;
    }

    private static Document tableHeavy() {
        // One large table with many rows. Tables register multiple resource
        // dictionaries and per-cell styling, so they exercise the resource
        // path that the optimizer compresses well.
        var doc = Document.builder()
            .letter()
            .title("Table-heavy fixture")
            .build();
        var builder = Table.builder()
            .autoColumnWidths()
            .headerRow("SKU", "Description", "Quantity", "Unit price", "Line total");
        for (int i = 1; i <= 60; i++) {
            builder.row(
                String.format("SKU-%04d", i),
                "Item description " + i,
                String.valueOf(i),
                String.format("$%d.99", i * 5),
                String.format("$%d.45", i * i * 5));
        }
        doc.add(builder.build());
        return doc;
    }

    /**
     * Builds the text-heavy document, writes it, parses the result, and
     * imports every page into a fresh Document. The output document carries
     * content streams in raw form — exactly the shape where Flate
     * recompression on write produces a large win.
     */
    private static Document importedTextHeavy() {
        byte[] sourceBytes;
        try (var src = textHeavy()) {
            sourceBytes = src.toBytes();
        }
        var out = Document.builder()
            .letter()
            .title("Imported text-heavy fixture")
            .build();
        try (var reader = PdfReader.parse(sourceBytes)) {
            for (int i = 0; i < reader.pageCount(); i++) {
                Page p = out.addPage();
                try (var imp = PageImport.from(reader, i)) {
                    imp.applyTo(p);
                }
            }
        }
        return out;
    }
}
