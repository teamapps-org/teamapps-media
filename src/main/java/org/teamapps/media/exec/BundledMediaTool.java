package org.teamapps.media.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

/** Architecture-aware alternative to {@link ExternalResource}; existing resource APIs are unchanged. */
public enum BundledMediaTool {
    FFMPEG("ffmpeg"), FFPROBE("ffprobe");

    private static final Logger LOGGER = LoggerFactory.getLogger(BundledMediaTool.class);
    private final String tool;
    private final ExecutableCache cache;

    BundledMediaTool(String tool) {
        this.tool = tool;
        this.cache = new ExecutableCache(this::extract);
    }

    /**
     * Extracts lazily, once per tool and library class loader. Concurrent first callers share
     * the same extraction. Subsequent calls return the cached path without filesystem access.
     * The private temporary directory is removed at normal JVM shutdown. An extraction failure
     * is not cached, so a later call can retry after the filesystem problem has been corrected.
     */
    public Path executable() throws IOException {
        return cache.get();
    }

    private Path extract() throws IOException {
        String os = System.getProperty("os.name", "");
        String arch = System.getProperty("os.arch", "");
        String resource = resourcePath(tool, os, arch);
        Path directory = Files.createTempDirectory("teamapps-media-tools-");
        Path binary = directory.resolve(tool + (resource.endsWith(".exe") ? ".exe" : ""));
        try (InputStream input = BundledMediaTool.class.getResourceAsStream(resource)) {
            if (input == null) throw new IOException("Missing TeamApps Media resource: " + resource);
            try (InputStream decoded = resource.endsWith(".gz") ? new GZIPInputStream(input) : input) {
                Files.copy(decoded, binary);
            }
            if (!binary.toFile().setExecutable(true, true) || !Files.isExecutable(binary)) {
                throw new IOException("Cannot make bundled " + tool + " executable; check temporary-directory permissions/noexec");
            }
            // deleteOnExit runs in reverse registration order.
            directory.toFile().deleteOnExit();
            binary.toFile().deleteOnExit();
            LOGGER.info("Bundled media tool ready: tool={}, os={}, arch={}, resource={}, binary={}", tool, os, arch, resource, binary);
            return binary;
        } catch (IOException | RuntimeException failure) {
            try { Files.deleteIfExists(binary); Files.deleteIfExists(directory); }
            catch (IOException cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
    }

    static String resourcePath(String tool, String os, String arch) throws IOException {
        os = os.toLowerCase(Locale.ROOT);
        arch = arch.toLowerCase(Locale.ROOT);
        boolean intel = arch.equals("amd64") || arch.equals("x86_64");
        boolean arm = arch.equals("aarch64") || arch.equals("arm64");
        String platform;
        if (os.contains("mac") && (intel || arm)) platform = arm ? "macos-arm64" : "macos-amd64";
        else if (os.contains("linux") && (intel || arm)) platform = arm ? "linux-arm64" : "linux-amd64";
        else if (os.startsWith("windows") && intel) return tool + "/" + tool + ".exe";
        else throw new IOException("No bundled " + tool + " for os=" + os + ", arch=" + arch);
        return "bundled/9.0.2/" + platform + "/" + tool + ".gz";
    }

    @FunctionalInterface
    interface Extractor { Path extract() throws IOException; }

    static final class ExecutableCache {
        private final Extractor extractor;
        private volatile Path executable;

        ExecutableCache(Extractor extractor) { this.extractor = extractor; }

        Path get() throws IOException {
            Path result = executable;
            if (result != null) return result;
            synchronized (this) {
                if (executable == null) executable = extractor.extract();
                return executable;
            }
        }
    }
}
