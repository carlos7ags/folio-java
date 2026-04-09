package dev.foliopdf;

/**
 * Unchecked exception thrown when a native Folio engine call fails due to
 * a file I/O error (C error code {@code FOLIO_ERR_IO = -3}).
 *
 * <p>This is a subclass of {@link FolioException}, so existing code that catches
 * {@code FolioException} continues to work. Callers who want finer-grained
 * handling of I/O errors can catch this type specifically:
 *
 * <pre>{@code
 * try {
 *     doc.save("/invalid/path.pdf");
 * } catch (FolioIOException e) {
 *     // handle file I/O failure specifically
 * } catch (FolioException e) {
 *     // handle other native failures
 * }
 * }</pre>
 */
public class FolioIOException extends FolioException {

    /**
     * Constructs an I/O exception with the given message.
     *
     * @param message the detail message
     */
    public FolioIOException(String message) {
        super(ErrorCode.IO.code(), message);
    }

    /**
     * Constructs an I/O exception with the given message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying throwable
     */
    public FolioIOException(String message, Throwable cause) {
        super(message, cause);
    }
}
