# Folio Java SDK — Claude Code Handoff

## Project Identity

- **Name:** Folio Java SDK
- **Repo:** https://github.com/folio-pdf/folio-java
- **Maven coordinates:** `io.folio:folio-java`
- **License:** Apache 2.0
- **Minimum JDK:** 21 (Panama FFI is preview in 21, stable in 22+)

---

## What This Is

A fluent Java API for PDF generation, backed by the Folio Go engine via Panama FFI.
The Go engine is compiled to a native shared library (`libfolio.so` / `libfolio.dylib` / `folio.dll`)
and this SDK calls it through Java's Foreign Function & Memory API (JEP 454).

---

## Architecture

```
User Java code
      ↓
io.folio.Document / Paragraph / Table / ...   ← fluent Java API (this repo)
      ↓
io.folio.internal.FolioNative                  ← Panama FFI bindings (auto-generated or manual)
      ↓
libfolio.so / libfolio.dylib / folio.dll       ← Go engine (compiled from folio-pdf/folio)
```

**Three layers:**
1. **Public API** (`io.folio.*`) — what developers import. Fluent builders, try-with-resources.
2. **Internal bindings** (`io.folio.internal.*`) — raw Panama method handles matching the C ABI.
3. **Native library** — bundled in JAR under `natives/{os}-{arch}/libfolio.{ext}`

---

## C ABI Reference

The Go engine exports 115 C functions. Full header: `folio-pdf/folio/export/folio.h`

### Key patterns:
- All objects are opaque `uint64_t` handles
- Functions return `int32_t` (0 = success, negative = error)
- `folio_last_error()` returns the error message (library-owned, don't free)
- `folio_version()` returns persistent string (don't free)
- Buffer data returned via handle: `folio_buffer_data/len/free`
- Strings passed to C are copied immediately

### Error codes:
```
FOLIO_OK            =  0
FOLIO_ERR_HANDLE    = -1
FOLIO_ERR_ARG       = -2
FOLIO_ERR_IO        = -3
FOLIO_ERR_PDF       = -4
FOLIO_ERR_TYPE      = -5
FOLIO_ERR_INTERNAL  = -6
```

### Core functions (most important for initial SDK):
```c
// Document lifecycle
uint64_t folio_document_new(double width, double height);
uint64_t folio_document_new_letter(void);
uint64_t folio_document_new_a4(void);
int32_t  folio_document_set_title(uint64_t doc, const char *title);
int32_t  folio_document_set_author(uint64_t doc, const char *author);
int32_t  folio_document_set_margins(uint64_t doc, double top, double right, double bottom, double left);
int32_t  folio_document_add(uint64_t doc, uint64_t element);
int32_t  folio_document_save(uint64_t doc, const char *path);
uint64_t folio_document_write_to_buffer(uint64_t doc);
void     folio_document_free(uint64_t doc);

// Layout elements
uint64_t folio_paragraph_new(const char *text, uint64_t font, double font_size);
int32_t  folio_paragraph_set_align(uint64_t para, int32_t align);
void     folio_paragraph_free(uint64_t para);

uint64_t folio_heading_new(const char *text, int32_t level);
void     folio_heading_free(uint64_t heading);

uint64_t folio_table_new(void);
uint64_t folio_table_add_row(uint64_t table);
uint64_t folio_row_add_cell(uint64_t row, const char *text, uint64_t font, double font_size);
void     folio_table_free(uint64_t table);

// Fonts
uint64_t folio_font_standard(const char *name);
uint64_t folio_font_helvetica(void);
uint64_t folio_font_load_ttf(const char *path);
void     folio_font_free(uint64_t font);

// HTML to PDF
int32_t  folio_html_to_pdf(const char *html, const char *output_path);
uint64_t folio_html_to_buffer(const char *html, double page_width, double page_height);

// Buffers
void    *folio_buffer_data(uint64_t buf);
int32_t  folio_buffer_len(uint64_t buf);
void     folio_buffer_free(uint64_t buf);
```

---

## Project Structure

```
folio-java/
├── settings.gradle.kts
├── lib/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/io/folio/
│       │   ├── Document.java          # top-level API, implements AutoCloseable
│       │   ├── PageSize.java          # A4, Letter, etc.
│       │   ├── Paragraph.java         # fluent paragraph builder
│       │   ├── Heading.java           # H1-H6
│       │   ├── HeadingLevel.java      # enum
│       │   ├── Table.java             # table builder
│       │   ├── Row.java               # table row
│       │   ├── Cell.java              # table cell
│       │   ├── Div.java               # container
│       │   ├── ListElement.java       # ordered/unordered list
│       │   ├── ListStyle.java         # enum
│       │   ├── Image.java             # image loading + element
│       │   ├── Font.java              # standard + embedded fonts
│       │   ├── Align.java             # enum: LEFT, CENTER, RIGHT, JUSTIFY
│       │   ├── Color.java             # RGB color
│       │   ├── HtmlConverter.java     # HTML → PDF
│       │   ├── PdfReader.java         # read existing PDFs
│       │   ├── FolioException.java    # runtime exception wrapping C errors
│       │   └── internal/
│       │       ├── FolioNative.java   # Panama FFI method handles
│       │       ├── HandleRef.java     # ref-counted handle + Cleaner integration
│       │       └── NativeLoader.java  # extracts libfolio from JAR, loads it
│       ├── main/resources/
│       │   └── natives/
│       │       ├── linux-x86_64/libfolio.so
│       │       ├── linux-aarch64/libfolio.so
│       │       ├── macos-x86_64/libfolio.dylib
│       │       ├── macos-aarch64/libfolio.dylib
│       │       └── windows-x86_64/folio.dll
│       └── test/java/io/folio/
│           ├── DocumentTest.java
│           ├── ParagraphTest.java
│           ├── TableTest.java
│           ├── HtmlConverterTest.java
│           └── PdfReaderTest.java
```

---

## Build Configuration

### build.gradle.kts (replace scaffold):
```kotlin
plugins {
    `java-library`
    `maven-publish`
}

group = "io.folio"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "Folio Java SDK"
                description = "Java PDF generation, signing, and processing library. Zero dependencies, Apache 2.0 licensed."
                url = "https://github.com/folio-pdf/folio-java"
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }
            }
        }
    }
}
```

### libs.versions.toml (replace scaffold):
```toml
[versions]
junit = "5.11.4"

[libraries]
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
```

---

## Target API — What Java Developers Will Write

```java
// Basic document
try (var doc = Document.builder()
        .pageSize(PageSize.A4)
        .margins(36, 36, 36, 36)
        .title("Q3 Report")
        .build()) {

    doc.add(Heading.of("Q3 Report", HeadingLevel.H1));

    doc.add(Paragraph.of("Revenue grew 23% year over year.")
        .fontSize(12)
        .align(Align.LEFT)
        .leading(1.5));

    doc.add(Table.builder()
        .columns(3)
        .headerRow("Product", "Units", "Revenue")
        .row("Widget A", "1,200", "$48,000")
        .row("Widget B", "850", "$34,000")
        .build());

    doc.save("report.pdf");
}

// HTML to PDF (one-liner)
HtmlConverter.toPdf("<h1>Invoice</h1><p>Due: $1,200</p>", "invoice.pdf");

// Read existing PDF
try (var reader = PdfReader.open("input.pdf")) {
    System.out.println("Pages: " + reader.pageCount());
    System.out.println("Text: " + reader.extractText(0));
}
```

---

## Implementation Plan

### Phase 1 — Panama FFI Foundation
1. `NativeLoader` — extract platform-specific `libfolio` from JAR resources, `System.load()`
2. `FolioNative` — static Panama method handles for all 115 C functions
3. `HandleRef` — thin wrapper around `uint64_t` handle with `Cleaner` integration for GC safety
4. `FolioException` — wraps error codes + `folio_last_error()` messages

### Phase 2 — Core API
5. `Document` — `AutoCloseable`, builder pattern, save/writeToBuffer
6. `Font` — standard fonts as constants, `Font.loadTTF(path)`
7. `Paragraph` — fluent setters, add to document
8. `Heading` — H1-H6 with alignment

### Phase 3 — Layout Elements
9. `Table` / `Row` / `Cell` — builder with header rows, cell styling
10. `Div` — container with padding/border/background
11. `ListElement` — ordered/unordered with style enum
12. `Image` — load JPEG/PNG, set size, add to flow

### Phase 4 — High-Level Features
13. `HtmlConverter` — static `toPdf()`, `toBuffer()`, `toDocument()`
14. `PdfReader` — open/parse, page count, text extraction
15. Forms API (if needed)

### Phase 5 — Distribution
16. Build native libs for all platforms (CI matrix)
17. Multi-architecture JAR with classifier
18. Publish to Maven Central

---

## Panama FFI Pattern

```java
// FolioNative.java (simplified example)
package io.folio.internal;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public final class FolioNative {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIB;

    static {
        NativeLoader.load();
        LIB = SymbolLookup.loaderLookup();
    }

    // folio_document_new_letter() -> uint64_t
    private static final MethodHandle document_new_letter = LINKER.downcallHandle(
        LIB.find("folio_document_new_letter").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_LONG)
    );

    public static long documentNewLetter() {
        try {
            return (long) document_new_letter.invokeExact();
        } catch (Throwable t) {
            throw new FolioException("documentNewLetter failed", t);
        }
    }

    // folio_document_save(uint64_t doc, const char* path) -> int32_t
    private static final MethodHandle document_save = LINKER.downcallHandle(
        LIB.find("folio_document_save").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
    );

    public static int documentSave(long doc, String path) {
        try (var arena = Arena.ofConfined()) {
            var cPath = arena.allocateFrom(path);
            return (int) document_save.invokeExact(doc, cPath);
        } catch (Throwable t) {
            throw new FolioException("documentSave failed", t);
        }
    }

    // ... 115 method handles total
}
```

---

## Key Design Decisions

1. **AutoCloseable everywhere** — Document, PdfReader, Font (embedded only) implement `AutoCloseable` to ensure `_free()` is called. Standard fonts are singletons, never closed.

2. **Cleaner as safety net** — `HandleRef` registers with `java.lang.ref.Cleaner` so handles are freed even if `close()` isn't called. But `close()` is preferred (deterministic).

3. **Arena per call** — Panama `Arena.ofConfined()` for string/buffer marshaling. Short-lived arenas keep memory bounded.

4. **Thread safety** — The Go C ABI is NOT thread-safe. `FolioNative` methods are synchronized on a global lock. This matches the Go side's mutex.

5. **No dependencies** — Zero runtime dependencies. Just JDK 21+ with Panama.

6. **Platform detection** — `NativeLoader` checks `os.name` + `os.arch` and extracts the right binary from JAR resources.

---

## Native Library Building

The Go engine must be cross-compiled for each target. In the `folio` (Go) repo:

```bash
# macOS arm64
CGO_ENABLED=1 GOOS=darwin GOARCH=arm64 go build -buildmode=c-shared -o libfolio.dylib ./export/

# macOS x86_64
CGO_ENABLED=1 GOOS=darwin GOARCH=amd64 go build -buildmode=c-shared -o libfolio.dylib ./export/

# Linux x86_64
CGO_ENABLED=1 GOOS=linux GOARCH=amd64 CC=x86_64-linux-gnu-gcc go build -buildmode=c-shared -o libfolio.so ./export/

# Linux aarch64
CGO_ENABLED=1 GOOS=linux GOARCH=arm64 CC=aarch64-linux-gnu-gcc go build -buildmode=c-shared -o libfolio.so ./export/

# Windows x86_64
CGO_ENABLED=1 GOOS=windows GOARCH=amd64 CC=x86_64-w64-mingw32-gcc go build -buildmode=c-shared -o folio.dll ./export/
```

These binaries are placed in `lib/src/main/resources/natives/{platform}/` in the Java repo.

---

## First Task for Claude Code

1. Replace the scaffold `org.example` package with `io.folio`
2. Set up `build.gradle.kts` with Java 21, Panama flags, zero deps
3. Implement `NativeLoader` — extract + load platform native lib
4. Implement `FolioNative` — Panama method handles for the core functions (document, paragraph, font, buffer)
5. Implement `Document` with builder, `AutoCloseable`, `save()`
6. Write `DocumentTest` that creates a blank PDF and verifies the file exists
7. Bundle the macOS arm64 `libfolio.dylib` for local testing
