/**
 * Folio Java SDK — PDF generation library backed by the Folio engine via Panama FFI.
 *
 * <p>Native access must be granted at runtime via the JVM flag:
 * {@code --enable-native-access=dev.foliopdf} (or {@code ALL-UNNAMED} for classpath usage).
 * This cannot be declared in module-info; it is a runtime configuration.
 */
module dev.foliopdf {
    requires java.logging;
    exports dev.foliopdf;
    // Internal package is exported but not part of the public API.
    // Users should not depend on it — it may change without notice.
    exports dev.foliopdf.internal;
}
