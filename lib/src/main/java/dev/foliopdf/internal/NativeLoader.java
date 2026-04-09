package dev.foliopdf.internal;

import dev.foliopdf.FolioException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public final class NativeLoader {

    private static final Logger LOG = Logger.getLogger(NativeLoader.class.getName());

    private static volatile boolean loaded = false;

    private NativeLoader() {}

    public static synchronized void load() {
        if (loaded) return;

        String platform = detectPlatform();
        String libName = libraryFileName();
        String resourcePath = "/natives/" + platform + "/" + libName;

        LOG.fine("Detected platform: " + platform);
        LOG.fine("Looking for native library resource: " + resourcePath);

        try (InputStream in = NativeLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                String msg = "Native library not found for platform: " + platform
                    + " (resource: " + resourcePath + ")";
                LOG.warning(msg);
                throw new FolioException(msg);
            }

            Path tempDir = Files.createTempDirectory("folio-native-");
            Path tempLib = tempDir.resolve(libName);
            Files.copy(in, tempLib, StandardCopyOption.REPLACE_EXISTING);
            tempLib.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();

            LOG.fine("Extracted native library to: " + tempLib.toAbsolutePath());

            System.load(tempLib.toAbsolutePath().toString());
            loaded = true;

            LOG.info("Folio native library loaded successfully [platform=" + platform
                + ", path=" + tempLib.toAbsolutePath() + "]");
        } catch (IOException e) {
            LOG.warning("Failed to extract native library: " + e.getMessage());
            throw new FolioException("Failed to extract native library", e);
        }
    }

    static String detectPlatform() {
        String os = System.getProperty("os.name", "").toLowerCase();
        String arch = System.getProperty("os.arch", "").toLowerCase();

        LOG.fine("os.name=" + os + ", os.arch=" + arch);

        String osKey;
        if (os.contains("mac") || os.contains("darwin")) {
            osKey = "macos";
        } else if (os.contains("linux")) {
            osKey = "linux";
        } else if (os.contains("win")) {
            osKey = "windows";
        } else {
            throw new FolioException("Unsupported OS: " + os);
        }

        String archKey;
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            archKey = "x86_64";
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            archKey = "aarch64";
        } else {
            throw new FolioException("Unsupported architecture: " + arch);
        }

        return osKey + "-" + archKey;
    }

    private static String libraryFileName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return "libfolio.dylib";
        } else if (os.contains("win")) {
            return "folio.dll";
        } else {
            return "libfolio.so";
        }
    }
}
