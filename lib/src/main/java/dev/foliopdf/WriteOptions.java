package dev.foliopdf;

import dev.foliopdf.internal.FolioNative;
import dev.foliopdf.internal.HandleRef;

/**
 * Configuration for the writer optimizer used by
 * {@link Document#saveWithOptions(String, WriteOptions)} and
 * {@link Document#toBytesWithOptions(WriteOptions)}.
 *
 * <p>Each toggle controls one feature of the PDF serializer:
 * <ul>
 *   <li><b>useXrefStream</b> — emit a cross-reference stream
 *       (ISO 32000-1 §7.5.8) instead of a classic {@code xref} table
 *       (§7.5.4). Required for object streams.</li>
 *   <li><b>useObjectStreams</b> — pack indirect objects into compressed
 *       object streams (§7.5.7). Implies {@code useXrefStream}.</li>
 *   <li><b>objectStreamCapacity</b> — maximum number of objects packed into
 *       a single object stream.</li>
 *   <li><b>orphanSweep</b> — drop indirect objects unreachable from the
 *       document catalog (§7.7.2) before writing.</li>
 *   <li><b>cleanContentStreams</b> — normalize and recompress content
 *       streams (§7.8) before writing.</li>
 *   <li><b>deduplicateObjects</b> — merge byte-identical indirect objects so
 *       they share a single object number (§7.3.10).</li>
 *   <li><b>recompressStreams</b> — re-encode existing flate streams with
 *       higher compression at write time.</li>
 * </ul>
 *
 * <p>Implements {@link AutoCloseable} — wrap with try-with-resources so the
 * underlying native handle is freed deterministically:
 *
 * <pre>{@code
 * try (var opts = WriteOptions.builder()
 *         .useXrefStream(true)
 *         .useObjectStreams(true)
 *         .deduplicateObjects(true)
 *         .build()) {
 *     doc.saveWithOptions("optimized.pdf", opts);
 * }
 * }</pre>
 *
 * <p>Both {@link Document#saveWithOptions(String, WriteOptions)} and
 * {@link Document#toBytesWithOptions(WriteOptions)} accept {@code null} as
 * "use defaults", so callers writing a single document with the optimizer
 * do not need to allocate a {@code WriteOptions} instance.
 *
 * @since 0.7.1
 */
public final class WriteOptions implements AutoCloseable {

    private final HandleRef handle;

    private WriteOptions(long handle) {
        this.handle = new HandleRef(handle, FolioNative::writeOptionsFree);
    }

    /**
     * Returns a new builder for constructing a {@link WriteOptions} instance.
     *
     * @return a fresh {@link Builder}
     * @since 0.7.1
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the native handle for these options. Used internally when
     * passing the options to writer entry points.
     *
     * @return the opaque native handle value
     * @since 0.7.1
     */
    public long handle() {
        return handle.get();
    }

    /**
     * Frees the underlying native options handle. Called automatically by
     * try-with-resources.
     *
     * @since 0.7.1
     */
    @Override
    public void close() {
        handle.close();
    }

    /**
     * Builder for {@link WriteOptions}. Obtain an instance from
     * {@link WriteOptions#builder()}.
     *
     * @since 0.7.1
     */
    public static final class Builder {
        private Boolean useXrefStream;
        private Boolean useObjectStreams;
        private Integer objectStreamCapacity;
        private Boolean orphanSweep;
        private Boolean cleanContentStreams;
        private Boolean deduplicateObjects;
        private Boolean recompressStreams;

        Builder() {}

        /**
         * Toggles emission of a cross-reference stream
         * (ISO 32000-1 §7.5.8) in place of a classic {@code xref} table
         * (§7.5.4).
         *
         * @param enabled {@code true} to write a cross-reference stream
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder useXrefStream(boolean enabled) {
            this.useXrefStream = enabled;
            return this;
        }

        /**
         * Toggles packing indirect objects into compressed object streams
         * (ISO 32000-1 §7.5.7). Implies {@link #useXrefStream(boolean)}.
         *
         * @param enabled {@code true} to use object streams
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder useObjectStreams(boolean enabled) {
            this.useObjectStreams = enabled;
            return this;
        }

        /**
         * Sets the maximum number of indirect objects packed into a single
         * object stream (ISO 32000-1 §7.5.7).
         *
         * @param capacity the per-stream object capacity (must be positive)
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder objectStreamCapacity(int capacity) {
            this.objectStreamCapacity = capacity;
            return this;
        }

        /**
         * Toggles dropping indirect objects that are unreachable from the
         * document catalog (ISO 32000-1 §7.7.2) before writing.
         *
         * @param enabled {@code true} to remove orphan objects
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder orphanSweep(boolean enabled) {
            this.orphanSweep = enabled;
            return this;
        }

        /**
         * Toggles normalizing and recompressing content streams
         * (ISO 32000-1 §7.8) prior to writing.
         *
         * @param enabled {@code true} to clean content streams
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder cleanContentStreams(boolean enabled) {
            this.cleanContentStreams = enabled;
            return this;
        }

        /**
         * Toggles merging of byte-identical indirect objects so they share a
         * single object number (ISO 32000-1 §7.3.10).
         *
         * @param enabled {@code true} to deduplicate objects
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder deduplicateObjects(boolean enabled) {
            this.deduplicateObjects = enabled;
            return this;
        }

        /**
         * Toggles re-encoding existing flate streams with higher compression
         * during the write pass.
         *
         * @param enabled {@code true} to recompress existing streams
         * @return this builder, for chaining
         * @since 0.7.1
         */
        public Builder recompressStreams(boolean enabled) {
            this.recompressStreams = enabled;
            return this;
        }

        /**
         * Allocates the native options handle and applies every configured
         * setting. Defaults are inherited from the native side for any setter
         * not invoked.
         *
         * @return a new {@link WriteOptions} instance
         * @throws FolioException if the native allocation fails
         * @since 0.7.1
         */
        public WriteOptions build() {
            long h = FolioNative.writeOptionsNew();
            if (h == 0) throw new FolioException("Failed to allocate WriteOptions: " + FolioNative.lastError());
            WriteOptions opts = new WriteOptions(h);
            try {
                if (useXrefStream != null) FolioNative.writeOptionsSetUseXrefStream(h, useXrefStream);
                if (useObjectStreams != null) FolioNative.writeOptionsSetUseObjectStreams(h, useObjectStreams);
                if (objectStreamCapacity != null) FolioNative.writeOptionsSetObjectStreamCapacity(h, objectStreamCapacity);
                if (orphanSweep != null) FolioNative.writeOptionsSetOrphanSweep(h, orphanSweep);
                if (cleanContentStreams != null) FolioNative.writeOptionsSetCleanContentStreams(h, cleanContentStreams);
                if (deduplicateObjects != null) FolioNative.writeOptionsSetDeduplicateObjects(h, deduplicateObjects);
                if (recompressStreams != null) FolioNative.writeOptionsSetRecompressStreams(h, recompressStreams);
            } catch (RuntimeException e) {
                opts.close();
                throw e;
            }
            return opts;
        }
    }
}
