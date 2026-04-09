package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * An OCSP (Online Certificate Status Protocol) client for checking
 * certificate revocation during signing. Required for PAdES B-LT and above.
 *
 * <pre>{@code
 * try (var ocsp = OcspClient.of()) {
 *     signer.sign(pdf, PadesLevel.B_LT, opts -> opts.ocsp(ocsp));
 * }
 * }</pre>
 */
public final class OcspClient implements AutoCloseable {

    private final HandleRef handle;

    private OcspClient(long handle) {
        this.handle = new HandleRef(handle, FolioNative::ocspClientFree);
    }

    /** Creates a new OCSP client. */
    public static OcspClient of() {
        long h = FolioNative.ocspClientNew();
        if (h == 0) throw new FolioException("Failed to create OCSP client: " + FolioNative.lastError());
        return new OcspClient(h);
    }

    long handle() {
        return handle.get();
    }

    @Override
    public void close() {
        handle.close();
    }
}
