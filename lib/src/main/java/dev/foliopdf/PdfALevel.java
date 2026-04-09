package dev.foliopdf;

/**
 * PDF/A conformance levels for long-term archival documents.
 *
 * <p>PDF/A-1 is based on PDF 1.4; PDF/A-2 and PDF/A-3 are based on PDF 1.7.
 * The suffix {@code A} (accessible) is a strict superset of {@code B} (basic) and {@code U} (unicode).
 */
public enum PdfALevel {
    PDF_A_2B(0),
    PDF_A_2U(1),
    PDF_A_2A(2),
    PDF_A_3B(3),
    PDF_A_1B(4),
    PDF_A_1A(5);

    private final int value;

    PdfALevel(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this PDF/A conformance level.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
