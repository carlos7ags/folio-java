package dev.foliopdf;

/**
 * PDF permission flags for encryption. Combine with bitwise OR.
 *
 * <pre>{@code
 * int perms = PdfPermission.PRINT | PdfPermission.EXTRACT;
 * doc.encryption("user", "owner", EncryptionAlgorithm.AES_256, perms);
 * }</pre>
 */
public final class PdfPermission {
    private PdfPermission() {}

    /** Allow printing. */
    public static final int PRINT = 1 << 2;
    /** Allow modifying contents. */
    public static final int MODIFY = 1 << 3;
    /** Allow copying/extracting text. */
    public static final int EXTRACT = 1 << 4;
    /** Allow annotations and form filling. */
    public static final int ANNOTATE = 1 << 5;
    /** Allow filling form fields. */
    public static final int FILL_FORMS = 1 << 8;
    /** Allow extraction for accessibility. */
    public static final int EXTRACT_ACCESS = 1 << 9;
    /** Allow inserting, rotating, deleting pages. */
    public static final int ASSEMBLE = 1 << 10;
    /** Allow high-quality printing. */
    public static final int PRINT_HIGH = 1 << 11;
    /** All permissions granted. */
    public static final int ALL = 0x0F3C;
}
