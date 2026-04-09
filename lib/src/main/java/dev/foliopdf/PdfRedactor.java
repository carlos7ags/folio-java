package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;

import java.util.List;

/**
 * Permanently removes sensitive content from PDF documents by painting
 * redaction rectangles over matched text, patterns, or explicit regions.
 *
 * <p>Use {@link RedactOpts} to configure fill color, overlay text, and metadata
 * stripping. All entry points return the redacted PDF bytes.
 *
 * <pre>{@code
 * try (var reader = PdfReader.open("report.pdf");
 *      var opts   = new RedactOpts().fillColor(0, 0, 0).stripMetadata(true)) {
 *     byte[] out = PdfRedactor.text(reader, List.of("123-45-6789"), opts);
 *     Files.write(Path.of("redacted.pdf"), out);
 * }
 * }</pre>
 */
public final class PdfRedactor {

    private PdfRedactor() {}

    /** Creates a new, fluent {@link RedactOpts} instance. */
    public static RedactOpts opts() {
        return new RedactOpts();
    }

    /**
     * Redacts every occurrence of each target string in the given PDF reader.
     *
     * @param reader  source PDF reader
     * @param targets literal strings to match and redact
     * @param opts    redaction appearance options
     * @return the redacted PDF bytes
     */
    public static byte[] text(PdfReader reader, List<String> targets, RedactOpts opts) {
        String[] arr = targets.toArray(new String[0]);
        long modifier = FolioNative.redactText(reader.readerHandle(), arr, opts.handle());
        if (modifier == 0) throw new FolioException("redactText failed: " + FolioNative.lastError());
        return writeModifierToBytes(modifier);
    }

    /**
     * Redacts every match of the given regular expression pattern.
     *
     * @param reader source PDF reader
     * @param regex  a regular expression used to locate redaction targets
     * @param opts   redaction appearance options
     * @return the redacted PDF bytes
     */
    public static byte[] pattern(PdfReader reader, String regex, RedactOpts opts) {
        long modifier = FolioNative.redactPattern(reader.readerHandle(), regex, opts.handle());
        if (modifier == 0) throw new FolioException("redactPattern failed: " + FolioNative.lastError());
        return writeModifierToBytes(modifier);
    }

    /**
     * Redacts the explicit rectangular regions supplied by the caller.
     *
     * @param reader  source PDF reader
     * @param regions list of page regions to black out
     * @param opts    redaction appearance options
     * @return the redacted PDF bytes
     */
    public static byte[] regions(PdfReader reader, List<RedactRegion> regions, RedactOpts opts) {
        int n = regions.size();
        int[] pages = new int[n];
        double[] x1s = new double[n];
        double[] y1s = new double[n];
        double[] x2s = new double[n];
        double[] y2s = new double[n];
        for (int i = 0; i < n; i++) {
            RedactRegion r = regions.get(i);
            pages[i] = r.page();
            x1s[i] = r.x1();
            y1s[i] = r.y1();
            x2s[i] = r.x2();
            y2s[i] = r.y2();
        }
        long modifier = FolioNative.redactRegions(reader.readerHandle(), pages, x1s, y1s, x2s, y2s, opts.handle());
        if (modifier == 0) throw new FolioException("redactRegions failed: " + FolioNative.lastError());
        return writeModifierToBytes(modifier);
    }

    private static byte[] writeModifierToBytes(long modifier) {
        try {
            long buf = FolioNative.mergeWriteToBuffer(modifier);
            if (buf == 0) throw new FolioException("Failed to write redacted PDF: " + FolioNative.lastError());
            return FolioNative.bufferToByteArray(buf);
        } finally {
            FolioNative.mergeFree(modifier);
        }
    }
}
