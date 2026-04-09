package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Signs PDF documents with PAdES-compliant digital signatures.
 *
 * <pre>{@code
 * byte[] key = Files.readAllBytes(Path.of("key.pem"));
 * byte[] cert = Files.readAllBytes(Path.of("cert.pem"));
 *
 * try (var signer = PdfSigner.fromPem(key, cert)) {
 *     byte[] signed = signer.sign(pdfBytes, PadesLevel.B_T, opts -> opts
 *         .name("Jane Doe")
 *         .reason("Approval")
 *         .location("Berlin"));
 *     Files.write(Path.of("signed.pdf"), signed);
 * }
 * }</pre>
 *
 * <p>Implements {@link AutoCloseable} — use try-with-resources.
 */
public final class PdfSigner implements AutoCloseable {

    private final HandleRef handle;

    private PdfSigner(long handle) {
        this.handle = new HandleRef(handle, FolioNative::signerFree);
    }

    /**
     * Creates a signer from PEM-encoded private key and certificate.
     *
     * @param keyPem  PEM-encoded private key bytes
     * @param certPem PEM-encoded certificate bytes (may include chain)
     * @return a new {@code PdfSigner}
     * @throws FolioException if the key or certificate cannot be parsed
     */
    public static PdfSigner fromPem(byte[] keyPem, byte[] certPem) {
        long h = FolioNative.signerNewPem(keyPem, certPem);
        if (h == 0) throw new FolioException("Failed to create signer from PEM: " + FolioNative.lastError());
        return new PdfSigner(h);
    }

    /**
     * Creates a signer from a PKCS#12 (.p12 / .pfx) keystore.
     *
     * @param data     the raw PKCS#12 bytes
     * @param password the keystore password (may be empty)
     * @return a new {@code PdfSigner}
     * @throws FolioException if the keystore cannot be parsed or the password is invalid
     */
    public static PdfSigner fromPkcs12(byte[] data, String password) {
        long h = FolioNative.signerNewPkcs12(data, password);
        if (h == 0) throw new FolioException("Failed to create signer from PKCS#12: " + FolioNative.lastError());
        return new PdfSigner(h);
    }

    /**
     * Signs a PDF with the given PAdES level and default options.
     *
     * @param pdfData the unsigned PDF bytes
     * @param level   the PAdES conformance level
     * @return the signed PDF bytes
     */
    public byte[] sign(byte[] pdfData, PadesLevel level) {
        return sign(pdfData, level, opts -> {});
    }

    /**
     * Signs a PDF with the given PAdES level and custom options.
     *
     * @param pdfData    the unsigned PDF bytes
     * @param level      the PAdES conformance level
     * @param configurer callback to configure signature options (name, reason, TSA, etc.)
     * @return the signed PDF bytes
     */
    public byte[] sign(byte[] pdfData, PadesLevel level, java.util.function.Consumer<SignOptions> configurer) {
        long opts = FolioNative.signOptsNew(handle.get(), level.value());
        if (opts == 0) throw new FolioException("Failed to create sign options: " + FolioNative.lastError());
        try {
            var signOpts = new SignOptions(opts);
            configurer.accept(signOpts);
            long buf = FolioNative.signPdf(pdfData, opts);
            if (buf == 0) throw new FolioException("Failed to sign PDF: " + FolioNative.lastError());
            try {
                int len = FolioNative.bufferLen(buf);
                MemorySegment data = FolioNative.bufferData(buf);
                return data.reinterpret(len).toArray(ValueLayout.JAVA_BYTE);
            } finally {
                FolioNative.bufferFree(buf);
            }
        } finally {
            FolioNative.signOptsFree(opts);
        }
    }

    @Override
    public void close() {
        handle.close();
    }

    /**
     * Configurable signature options passed to the sign callback.
     * Set name, reason, location, contact info, TSA, and OCSP.
     */
    public static final class SignOptions {
        private final long handle;

        SignOptions(long handle) {
            this.handle = handle;
        }

        /** Sets the signer's name. */
        public SignOptions name(String name) {
            FolioNative.signOptsSetName(handle, name);
            return this;
        }

        /** Sets the reason for signing. */
        public SignOptions reason(String reason) {
            FolioNative.signOptsSetReason(handle, reason);
            return this;
        }

        /** Sets the signing location. */
        public SignOptions location(String location) {
            FolioNative.signOptsSetLocation(handle, location);
            return this;
        }

        /** Sets contact information. */
        public SignOptions contactInfo(String info) {
            FolioNative.signOptsSetContactInfo(handle, info);
            return this;
        }

        /** Sets a TSA (Time Stamp Authority) client for timestamped signatures. */
        public SignOptions tsa(TsaClient tsa) {
            FolioNative.signOptsSetTsa(handle, tsa.handle());
            return this;
        }

        /** Sets an OCSP client for revocation checking. */
        public SignOptions ocsp(OcspClient ocsp) {
            FolioNative.signOptsSetOcsp(handle, ocsp.handle());
            return this;
        }
    }
}
