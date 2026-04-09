package dev.foliopdf;

/**
 * Structured error codes returned by the native Folio engine.
 * Retrieve from a caught {@link FolioException} via {@link FolioException#error()}.
 *
 * <pre>{@code
 * try {
 *     doc.save("/invalid/path.pdf");
 * } catch (FolioException e) {
 *     if (e.error() == ErrorCode.IO) {
 *         // handle I/O error
 *     }
 * }
 * }</pre>
 */
public enum ErrorCode {
    /** No error or error code not applicable. */
    NONE(0),
    /** Invalid or expired native handle. */
    HANDLE(-1),
    /** Invalid argument (null, out of range). */
    ARG(-2),
    /** File I/O error. */
    IO(-3),
    /** PDF generation or parsing error. */
    PDF(-4),
    /** Handle type mismatch (e.g., passing a font handle where a document handle is expected). */
    TYPE(-5),
    /** Unexpected internal error in the native engine. */
    INTERNAL(-6);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    /** Returns the raw C ABI integer code. */
    public int code() {
        return code;
    }

    /**
     * Resolves a raw C error code to the corresponding enum constant.
     *
     * @param code the raw integer error code
     * @return the matching {@link ErrorCode}, or {@link #NONE} if unrecognized
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode ec : values()) {
            if (ec.code == code) return ec;
        }
        return NONE;
    }
}
