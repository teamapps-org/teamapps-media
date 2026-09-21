package org.teamapps.media.exec;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class BundledMediaToolTest {
    @Test public void concurrentFirstCallsExtractExactlyOnceAndLaterCallsDoNoFilesystemWork() throws Exception {
        AtomicInteger extractions = new AtomicInteger();
        Path path = Path.of("does-not-exist", "tool");
        BundledMediaTool.ExecutableCache cache = new BundledMediaTool.ExecutableCache(() -> {
            extractions.incrementAndGet();
            return path;
        });
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(16)) {
            List<Future<Path>> results = new ArrayList<>();
            for (int i = 0; i < 64; i++) results.add(executor.submit(() -> { start.await(); return cache.get(); }));
            start.countDown();
            for (Future<Path> result : results) assertSame(path, result.get());
        }
        for (int i = 0; i < 1000; i++) assertSame(path, cache.get());
        assertEquals(1, extractions.get());
    }

    @Test public void failedExtractionCanBeRetriedButSuccessIsCached() throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        BundledMediaTool.ExecutableCache cache = new BundledMediaTool.ExecutableCache(() -> {
            if (attempts.incrementAndGet() == 1) throw new IOException("temporary failure");
            return Path.of("tool");
        });
        assertThrows(IOException.class, cache::get);
        assertSame(cache.get(), cache.get());
        assertEquals(2, attempts.get());
    }

    @Test public void platformSelectionDoesNotConfuseArchitecturesOrUnknownOperatingSystems() throws Exception {
        assertNotEquals(BundledMediaTool.resourcePath("ffmpeg", "Mac OS X", "aarch64"), BundledMediaTool.resourcePath("ffmpeg", "Mac OS X", "x86_64"));
        assertNotEquals(BundledMediaTool.resourcePath("ffmpeg", "Linux", "arm64"), BundledMediaTool.resourcePath("ffmpeg", "Linux", "amd64"));
        for (String os : List.of("Mac OS X", "Linux")) {
            for (String arch : List.of("aarch64", "amd64")) {
                for (String tool : List.of("ffmpeg", "ffprobe")) {
                    try (var stream = BundledMediaTool.class.getResourceAsStream(BundledMediaTool.resourcePath(tool, os, arch))) {
                        assertNotNull(os + " " + arch + " " + tool, stream);
                    }
                }
            }
        }
        assertThrows(IOException.class, () -> BundledMediaTool.resourcePath("ffmpeg", "Darwin", "riscv64"));
        assertThrows(IOException.class, () -> BundledMediaTool.resourcePath("ffmpeg", "Windows 11", "aarch64"));
    }

    @Test public void bundledExecutablesRunAndSupportRequiredAudioAndVideoCodecs() throws Exception {
        for (BundledMediaTool tool : BundledMediaTool.values()) {
            Path executable = tool.executable();
            assertSame(executable, tool.executable());
            assertTrue(executable.isAbsolute());
            assertTrue(Files.isExecutable(executable));
            var result = MediaCommandExecutor.execute(List.of(executable.toString(), "-version"), Duration.ofSeconds(20));
            assertTrue(result.stdout().contains(" version "));
        }
        var codecs = MediaCommandExecutor.execute(List.of(BundledMediaTool.FFMPEG.executable().toString(), "-hide_banner", "-encoders"), Duration.ofSeconds(20));
        for (String codec : List.of("libmp3lame", "libx264", "libopus", "aac")) assertTrue(codec, codecs.stdout().contains(codec));
    }
}
