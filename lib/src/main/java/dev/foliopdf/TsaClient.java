package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * A Time Stamp Authority (TSA) client for adding trusted timestamps
 * to digital signatures. Required for PAdES B-T and above.
 *
 * <pre>{@code
 * try (var tsa = TsaClient.of("http://timestamp.digicert.com")) {
 *     signer.sign(pdf, PadesLevel.B_T, opts -> opts.tsa(tsa));
 * }
 * }</pre>
 */
public final class TsaClient implements AutoCloseable {

    private final HandleRef handle;

    private TsaClient(long handle) {
        this.handle = new HandleRef(handle, FolioNative::tsaClientFree);
    }

    /**
     * Creates a TSA client pointing to the given URL.
     *
     * @param url the TSA service URL
     * @return a new {@code TsaClient}
     */
    public static TsaClient of(String url) {
        long h = FolioNative.tsaClientNew(url);
        if (h == 0) throw new FolioException("Failed to create TSA client: " + FolioNative.lastError());
        return new TsaClient(h);
    }

    long handle() {
        return handle.get();
    }

    @Override
    public void close() {
        handle.close();
    }
}
