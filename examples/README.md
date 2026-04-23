# Examples

Each class is a self-contained example that produces a PDF.

## Running

```bash
# Default (Hello)
./gradlew examples:run

# Specific example
./gradlew examples:run -PmainClass=dev.foliopdf.examples.HtmlToPdf
```

## Structure

```
examples/src/main/java/dev/foliopdf/examples/
├── Hello.java          # minimal one-page PDF
├── Fonts.java          # standard, custom, and Unicode fonts
├── Links.java          # hyperlinks, bookmarks, internal navigation
├── Forms.java          # interactive AcroForm fields (text, checkbox, radio, dropdown)
├── HtmlToPdf.java      # rich HTML+CSS report (flexbox, tables, page breaks)
├── Cjk.java            # Chinese, Japanese, Korean text + kinsoku shori
├── Rtl.java            # Hebrew bidi, Arabic shaping, kashida, ActualText
├── Indic.java          # Devanagari shaping (reph, conjuncts, half forms, nukta)
├── Optimize.java       # WriteOptions optimizer comparison across fixtures
└── ZugferdInvoice.java # PDF/A-3B invoice with Factur-X XML attachment
```
