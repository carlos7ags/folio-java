# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Bundled folio engine bumped to v0.7.1 (16 new C ABI exports)
- `WriteOptions` builder + `Document.saveWithOptions()` /
  `Document.toBytesWithOptions()` for the writer optimizer (xref streams,
  object streams, orphan sweep, content-stream cleanup, object
  deduplication, stream recompression). Both writer entry points accept
  `null` for "use defaults"
- `Direction` enum (`AUTO`, `LTR`, `RTL`) with `setDirection(...)` on
  `Paragraph`, `ListElement`, and `Table`
- `Document.setActualText(boolean)` to opt out of `/ActualText` emission
  in tagged PDFs
- `Columns.setBalanced(boolean)` toggle for balanced vs sequential
  multi-column fill

## [0.1.0] - 2026-04-09

First stable release of the Folio Java SDK, bundling the folio Go engine v0.6.2.

### Added

- 372 Panama FFI bindings to the folio Go engine (`dev.foliopdf.internal.FolioNative`)
- 59 public API classes with fluent builders (`dev.foliopdf.*`)
- Zero runtime dependencies -- requires JDK 22+ only
- 5-platform native library bundled in the JAR: linux-x86_64, linux-aarch64, macos-x86_64, macos-aarch64, windows-x86_64
- Document generation: paragraphs, headings, tables, divs, lists, images, links, line separators, area breaks
- Advanced layout: Flexbox (`Flex`), CSS Grid (`Grid`), multi-column (`Columns`), float (`FloatElement`), tabbed lines (`TabbedLine`)
- Barcodes: QR, Code128, EAN-13 (`Barcode`)
- SVG parsing and rendering (`SvgElement`)
- HTML-to-PDF conversion as a one-liner (`HtmlConverter.toPdf()`)
- PDF reading: text extraction, metadata, page dimensions, structure tree (`PdfReader`)
- PDF merge with page manipulation -- rotate, reorder, crop, remove (`PdfMerger`)
- Page import for template workflows (`PageImport`)
- Digital signatures: PAdES B-B through B-LTA with PEM and PKCS#12 support (`PdfSigner`, `PadesLevel`, `TsaClient`, `OcspClient`)
- Form creation, filling, and flattening (`Form`, `FormField`, `FormFiller`)
- PDF redaction -- text, regex, and region-based (`PdfRedactor`, `RedactRegion`, `RedactOpts`)
- Encryption with granular permissions using AES-256, AES-128, or RC4 (`EncryptionAlgorithm`, `PdfPermission`)
- PDF/A compliance at levels 1-3 (B/U/A) with validation (`PdfALevel`)
- Tagged PDF / PDF/UA support: alt text, custom structure tags, structure tree reading
- Drawing primitives on pages: lines, rectangles (`Page`)
- Text highlighting via TextRun background color
- CSS length parsing utility (`CssLength`)
- Headers and footers with page number placeholders (`PageDecorator`)
- `OutputStream`, `InputStream`, and `Path` API support on `Document`, `PdfReader`, `HtmlConverter`, and `Font`
- 12 runnable examples: Hello, Report, Invoice, HtmlToPdf, Merge, Forms, Fonts, Links, ZugferdInvoice, Redact, ImportPage, Sign

[0.1.0]: https://github.com/carlos7ags/folio-java/releases/tag/v0.1.0
