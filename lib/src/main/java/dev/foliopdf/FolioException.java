package dev.foliopdf;

/**
 * Unchecked exception thrown when a native Folio engine call fails.
 * The message describes the failure; {@link #errorCode()} returns the
 * raw C error code when one is available (0 if not applicable).
 *
 * <p>Error codes map to the C ABI constants:
 * {@code FOLIO_ERR_HANDLE (-1)}, {@code FOLIO_ERR_ARG (-2)},
 * {@code FOLIO_ERR_IO (-3)}, {@code FOLIO_ERR_PDF (-4)},
 * {@code FOLIO_ERR_TYPE (-5)}, {@code FOLIO_ERR_INTERNAL (-6)}.
 */
public class FolioException extends RuntimeException {

    private final int errorCode;

    /**
     * Constructs an exception with the given message and no error code.
     *
     * @param message the detail message
     */
    public FolioException(String message) {
        super(message);
        this.errorCode = 0;
    }

    /**
     * Constructs an exception with the given message and a cause.
     *
     * @param message the detail message
     * @param cause   the underlying throwable
     */
    public FolioException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = 0;
    }

    /**
     * Constructs an exception with a native C error code and a detail message.
     *
     * @param errorCode the raw C ABI error code (negative integer)
     * @param message   the detail message from {@code folio_last_error()}
     */
    public FolioException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Returns the raw C ABI error code, or {@code 0} if no code was provided.
     *
     * @return the error code
     */
    public int errorCode() {
        return errorCode;
    }

    /**
     * Returns the structured {@link ErrorCode} for this exception.
     * More convenient than comparing raw integer codes.
     *
     * @return the error code enum, or {@link ErrorCode#NONE} if not applicable
     */
    public ErrorCode error() {
        return ErrorCode.fromCode(errorCode);
    }
}
