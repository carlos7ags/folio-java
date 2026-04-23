# Folio Java SDK

[![CI](https://github.com/carlos7ags/folio-java/actions/workflows/ci.yml/badge.svg)](https://github.com/carlos7ags/folio-java/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.foliopdf/folio-java)](https://central.sonatype.com/artifact/dev.foliopdf/folio-java)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-22%2B-orange.svg)](https://openjdk.org/projects/jdk/22/)

A fluent Java API for PDF generation, signing, and processing. Backed by the [Folio](https://github.com/carlos7ags/folio) Go engine via [Panama FFI](https://openjdk.org/jeps/454). Apache 2.0 licensed.

**Requires JDK 22+** | Zero runtime dependencies | Bundles folio engine v0.7.0 | [foliopdf.dev](https://foliopdf.dev) | [Playground](https://playground.foliopdf.dev)

## Requirements

Folio uses the [Foreign Function & Memory API](https://openjdk.org/jeps/454) (Panama FFI) to call into the native engine. This requires two things:

1. **JDK 22 or later** (Temurin, Corretto, GraalVM, or any OpenJDK distribution)
2. The JVM flag `--enable-native-access=ALL-UNNAMED`

**Gradle:**
```kotlin
tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}
```

**Maven (Surefire / exec-maven-plugin):**
```xml
<argLine>--enable-native-access=ALL-UNNAMED</argLine>
```

**Command line:**
```bash
java --enable-native-access=ALL-UNNAMED -jar myapp.jar
```

Native libraries for Linux (x86_64, ARM64), macOS (x86_64, ARM64), and Windows (x86_64) are bundled in the JAR and extracted automatically at runtime.

## Quick start

```xml
<!-- Maven -->
<dependency>
    <groupId>dev.foliopdf</groupId>
    <artifactId>folio-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

```kotlin
// Gradle
implementation("dev.foliopdf:folio-java:0.1.0")
```

## Usage

### Create a PDF

The simplest way to create a PDF:

```java
Document.create("report.pdf", doc -> {
    doc.add(Heading.of("Q3 Report", HeadingLevel.H1));
    doc.add(Paragraph.of("Revenue grew 23% year over year."));
    doc.add(Table.of(
        new String[]{"Product", "Units", "Revenue"},
        new String[]{"Widget A", "1,200", "$48,000"},
        new String[]{"Widget B", "850", "$34,000"}
    ));
});
```

For more control, use the builder:

```java
try (var doc = Document.builder()
        .a4()
        .title("Q3 Report")
        .margins(36)
        .build()) {

    doc.add(Heading.of("Q3 Report", HeadingLevel.H1));
    doc.add(Paragraph.of("Revenue grew 23% year over year."));
    doc.save("report.pdf");
}
```

### HTML to PDF

```java
HtmlConverter.toPdf("<h1>Invoice</h1><p>Due: $1,200</p>", "invoice.pdf");

// Or get bytes directly for HTTP responses
byte[] pdf = HtmlConverter.toBytes("<h1>Hello</h1><p>World</p>");
```

### Read an existing PDF

```java
try (var reader = PdfReader.open("input.pdf")) {
    System.out.println("Pages: " + reader.pageCount());
    System.out.println("Title: " + reader.title());
    System.out.println("Text: " + reader.extractAllText());
}
```

### Merge PDFs

```java
PdfMerger.merge("annual.pdf", "q3-report.pdf", "q4-report.pdf");
```

Or with more control:

```java
try (var r1 = PdfReader.open("report-q3.pdf");
     var r2 = PdfReader.open("report-q4.pdf");
     var merged = PdfMerger.merge(r1, r2)) {
    merged.setInfo("Annual Report", "ACME Corp");
    merged.save("annual.pdf");
}
```

### Digital signatures

Sign with PAdES B-B through B-LTA:

```java
try (var signer = PdfSigner.fromPem(keyPem, certPem)) {
    byte[] signed = signer.sign(pdfBytes, PadesLevel.B_B, opts -> opts
        .name("Jane Doe")
        .reason("Approval")
        .location("Berlin"));
    Files.write(Path.of("signed.pdf"), signed);
}
```

### Redaction

Permanently remove sensitive text from PDFs:

```java
try (var reader = PdfReader.open("confidential.pdf");
     var opts = PdfRedactor.opts().fillColor(0, 0, 0).stripMetadata(true)) {
    byte[] redacted = PdfRedactor.text(reader, List.of("SSN: 123-45-6789"), opts);
    Files.write(Path.of("redacted.pdf"), redacted);
}
```

### Headers and footers

```java
doc.setHeaderText("CONFIDENTIAL", Font.helvetica(), 9, Align.RIGHT);
doc.setFooterText("Page {page} of {pages}", Font.helvetica(), 8, Align.CENTER);
```

For full control, use callbacks:

```java
doc.header((pageIndex, totalPages, page) ->
    page.addText("Custom header", Font.helvetica(), 9, 72, 20));
```

### Flexbox layout

```java
var flex = Flex.of()
    .direction(FlexDirection.ROW)
    .gap(10)
    .justifyContent(JustifyContent.SPACE_BETWEEN);
flex.add(Paragraph.of("Left"));
flex.add(Paragraph.of("Right"));
doc.add(flex);
```

### Grid layout

```java
var grid = Grid.of()
    .templateColumns(
        new GridTrackType[]{GridTrackType.FR, GridTrackType.FR},
        new double[]{1, 2})
    .gap(10, 10);
grid.addChild(Paragraph.of("Narrow column"));
grid.addChild(Paragraph.of("Wide column"));
doc.add(grid);
```

### Barcodes and QR codes

```java
try (var qr = Barcode.qr("https://example.com", 150)) {
    qr.align(Align.CENTER);
    doc.add(qr);
}
```

### SVG

```java
try (var svg = SvgElement.parse("<svg>...</svg>")) {
    svg.size(200, 200);
    doc.add(svg);
}
```

### Forms

```java
try (var form = Form.of()) {
    form.addTextField("name", 72, 700, 300, 720, 0);
    form.addCheckbox("agree", 72, 680, 90, 695, 0, false);
    doc.form(form);
}
```

### PDF/A and encryption

```java
doc.pdfA(PdfALevel.PDF_A_3B);
doc.encryption("user-pass", "owner-pass", EncryptionAlgorithm.AES_256);
```

### Writer optimizer (v0.7.0)

`WriteOptions` exposes the per-feature toggles of the v0.7.0 PDF writer:
cross-reference streams (ISO 32000-1 §7.5.8), object streams (§7.5.7),
orphan sweep, content-stream cleanup, object deduplication, and stream
recompression.

```java
try (var doc = Document.builder().a4().build();
     var opts = WriteOptions.builder()
         .useXrefStream(true)
         .useObjectStreams(true)
         .objectStreamCapacity(64)
         .deduplicateObjects(true)
         .recompressStreams(true)
         .build()) {
    doc.add(Paragraph.of("Hello"));
    doc.saveWithOptions("optimized.pdf", opts);
}
```

Both `Document.saveWithOptions(...)` and `Document.toBytesWithOptions(...)`
accept `null` for the options argument to use engine defaults, so callers
that want a single optimized write do not need to allocate a `WriteOptions`
instance.

### Right-to-left text (v0.7.0)

Set the writing direction on paragraphs, lists, or tables with the
`Direction` enum. `AUTO` defers to the Unicode Bidi algorithm; `LTR` and
`RTL` force the direction.

```java
doc.add(Paragraph.of("שלום עולם").setDirection(Direction.RTL));
doc.add(ListElement.of().setDirection(Direction.AUTO).item("بند"));

var table = Table.builder().row("الأول", "الثاني", "الثالث").build();
table.setDirection(Direction.RTL);
doc.add(table);
```

RTL tables reverse the visual column order; RTL lists place markers on
the right.

### `/ActualText` toggle (v0.7.0)

Tagged-PDF output emits `/ActualText` entries on marked-content sequences
by default (ISO 32000-1 §14.9.4). Disable them when accessibility is not
required to reduce file size.

```java
doc.tagged(true);
doc.setActualText(false);
```

### Balanced multi-column fill (v0.7.0)

`Columns.setBalanced(true)` fills columns to roughly equal heights,
matching the CSS `column-fill: balance` model. The default is sequential
fill (each column reaches full height before the next begins).

```java
var cols = Columns.of(2).gap(20).setBalanced(true);
cols.add(0, Paragraph.of("First fragment"));
cols.add(1, Paragraph.of("Second fragment"));
doc.add(cols);
```

## Features

| Feature | Status |
|---------|--------|
| Document creation (paragraphs, headings, tables, images) | ✅ |
| Fluent builder API with `Document.create()` one-liner | ✅ |
| HTML to PDF conversion | ✅ |
| PDF reading and text extraction | ✅ |
| PDF merging with page manipulation | ✅ |
| Digital signatures (PAdES B-B through B-LTA) | ✅ |
| PDF redaction (text, regex, regions) | ✅ |
| Tagged PDF / PDF/UA accessibility | ✅ |
| Page import for template workflows | ✅ |
| Flexbox and CSS Grid layout | ✅ |
| Multi-column layout (balanced or sequential) | ✅ |
| Writer optimizer (xref streams, object streams, dedup) | ✅ |
| Right-to-left text (paragraph, list, table) | ✅ |
| `/ActualText` toggle for tagged PDFs | ✅ |
| Barcodes (QR, Code128, EAN-13) | ✅ |
| SVG rendering | ✅ |
| Hyperlinks and internal navigation | ✅ |
| Interactive forms (AcroForm) | ✅ |
| Form filling and flattening | ✅ |
| PDF/A compliance (1a/1b/2a/2b/2u/3b) | ✅ |
| Password encryption (RC4, AES-128/256) | ✅ |
| Granular permission flags | ✅ |
| Watermarks | ✅ |
| Headers and footers (text and callbacks) | ✅ |
| Bookmarks and outlines | ✅ |
| File attachments (PDF/A-3b) | ✅ |
| Drawing primitives (lines, rectangles) | ✅ |
| All 14 standard PDF fonts | ✅ |
| Custom TTF font embedding | ✅ |
| JPMS module support | ✅ |
| Thread safety | ✅ |
| Zero runtime dependencies | ✅ |

## Element types

All layout elements implement the `Element` interface and can be added to a `Document`, `Div`, `Flex`, or `Grid`:

`Paragraph` · `Heading` · `Table` · `Image` · `Div` · `ListElement` · `Link` · `Barcode` · `SvgElement` · `Flex` · `Grid` · `Columns` · `FloatElement` · `TabbedLine` · `LineSeparator` · `AreaBreak`

## Examples

There are 12 runnable examples in the [`examples/`](examples/) directory:

```bash
./gradlew examples:run                                                    # Hello world
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Report           # Multi-page report
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Invoice          # HTML invoice
./gradlew examples:run -PmainClass=dev.foliopdf.examples.HtmlToPdf        # HTML to PDF
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Merge            # PDF merging
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Sign             # Digital signatures
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Redact           # PDF redaction
./gradlew examples:run -PmainClass=dev.foliopdf.examples.ImportPage       # Page import templates
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Forms            # Interactive forms
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Fonts            # Font showcase
./gradlew examples:run -PmainClass=dev.foliopdf.examples.Links            # Hyperlinks and bookmarks
./gradlew examples:run -PmainClass=dev.foliopdf.examples.ZugferdInvoice   # PDF/A-3B invoice
```

## Architecture

```
Your Java code
      ↓
dev.foliopdf.*                ← fluent public API (this SDK)
      ↓
dev.foliopdf.internal.*       ← Panama FFI bindings (372 method handles)
      ↓
libfolio.so / .dylib / .dll   ← Folio Go engine (bundled in JAR)
```

All calls into the native library are serialized through a lock for thread safety.

## Building from source

```bash
git clone https://github.com/carlos7ags/folio-java.git
cd folio-java
./gradlew build
```

## License

Apache 2.0. See [LICENSE](LICENSE).
