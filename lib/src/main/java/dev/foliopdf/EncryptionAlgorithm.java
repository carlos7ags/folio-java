package dev.foliopdf;

/**
 * Encryption algorithms available for PDF password protection.
 *
 * <p>{@link #AES_256} is recommended for new documents; {@link #RC4_128} is provided for
 * compatibility with older PDF readers.
 */
public enum EncryptionAlgorithm {
    RC4_128(0),
    AES_128(1),
    AES_256(2);

    private final int value;

    EncryptionAlgorithm(int value) {
        this.value = value;
    }

    /**
     * Returns the native integer code for this encryption algorithm.
     *
     * @return the native integer constant
     */
    public int value() {
        return value;
    }
}
