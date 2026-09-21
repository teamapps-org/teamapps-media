package org.teamapps.media.exec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * Opt-in, synchronous process API. Arguments are passed individually, without shell parsing.
 * Non-zero exit codes throw, both output streams are drained, stdin is closed, and cancellation
 * or timeout terminates the process. Existing {@link CommandLineExecutor} behavior is unchanged.
 * Output tails are bounded; use the file overload when complete stdout (e.g. JSON) is required.
 */
public final class MediaCommandExecutor {
    private MediaCommandExecutor() { }

    public record Result(int exitCode, String stdout, String stderr, Duration elapsed) { }

    public static Result execute(List<String> command, Duration timeout) throws IOException, InterruptedException {
        return execute(command, null, timeout, () -> false);
    }

    /**
     * @param stdoutFile optional stdout destination, truncated on start; caller owns this file
     * @param timeout positive limit including reading the final output
     * @param cancelled polled at least every 100 ms while waiting; must be fast and thread-safe
     * @return successful result (exit code zero), with at most 64 KiB stdout / 4 KiB stderr;
     *         stdout is empty when redirected to a file
     * @throws CancellationException if the cancellation predicate returns true
     * @throws InterruptedException if interrupted; the interrupt flag is restored after cleanup
     * @throws MediaExecutionException on start, exit-code, timeout or output failure
     */
    public static Result execute(List<String> command, Path stdoutFile, Duration timeout, BooleanSupplier cancelled)
            throws IOException, InterruptedException {
        List<String> args = List.copyOf(command);
        if (args.isEmpty() || args.getFirst().isBlank()) throw new IllegalArgumentException("Executable is required");
        Objects.requireNonNull(cancelled, "cancelled");
        long limit = Objects.requireNonNull(timeout, "timeout").toNanos();
        if (limit <= 0) throw new IllegalArgumentException("Timeout must be positive");
        checkCancellation(cancelled);
        long start = System.nanoTime();
        String binary = args.getFirst();
        ProcessBuilder builder = new ProcessBuilder(args);
        if (stdoutFile != null) builder.redirectOutput(stdoutFile.toFile());
        Process process;
        try { process = builder.start(); }
        catch (IOException failure) {
            throw new MediaExecutionException(MediaExecutionException.Reason.START_FAILED,
                    "Cannot start media tool " + binary + "; check binary availability, architecture, execution permissions and temporary-directory noexec",
                    null, "", failure);
        }
        Capture stdout = new Capture(process.getInputStream(), 65536, "stdout");
        Capture stderr = new Capture(process.getErrorStream(), 4096, "stderr");
        boolean interrupted = false;
        try {
            process.getOutputStream().close();
            while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
                checkCancellation(cancelled);
                checkTimeout(start, limit, timeout, binary, stderr);
            }
            // Join readers before inspecting output, avoiding incomplete JSON and truncated errors.
            while (stdout.thread.isAlive() || stderr.thread.isAlive()) {
                checkCancellation(cancelled);
                checkTimeout(start, limit, timeout, binary, stderr);
                stdout.thread.join(25);
                stderr.thread.join(25);
            }
            checkCancellation(cancelled);
            checkTimeout(start, limit, timeout, binary, stderr);
            int exitCode = process.exitValue();
            if (exitCode != 0) throw new MediaExecutionException(MediaExecutionException.Reason.NON_ZERO_EXIT,
                    "Media tool failed; binary=" + binary + ", exit=" + exitCode + ", stderr=" + diagnostic(stderr.text()), exitCode, stderr.text(), null);
            IOException readFailure = stdout.failure != null ? stdout.failure : stderr.failure;
            if (readFailure != null) throw new MediaExecutionException(MediaExecutionException.Reason.OUTPUT_FAILED,
                    "Cannot read media tool output; binary=" + binary, exitCode, stderr.text(), readFailure);
            return new Result(exitCode, stdout.text(), stderr.text(), Duration.ofNanos(System.nanoTime() - start));
        } catch (InterruptedException failure) {
            interrupted = true;
            throw failure;
        } finally {
            // Cleanup must finish even when the caller was interrupted.
            interrupted |= Thread.interrupted();
            if (process.isAlive()) process.destroyForcibly();
            long cleanupStart = System.nanoTime();
            while (process.isAlive() && System.nanoTime() - cleanupStart < TimeUnit.SECONDS.toNanos(5)) {
                try { process.waitFor(100, TimeUnit.MILLISECONDS); }
                catch (InterruptedException ignored) { interrupted = true; }
            }
            close(process.getInputStream());
            close(process.getErrorStream());
            try { process.getOutputStream().close(); } catch (IOException ignored) { }
            if (interrupted) Thread.currentThread().interrupt();
        }
    }

    private static void checkCancellation(BooleanSupplier cancelled) throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Media processing interrupted");
        if (cancelled.getAsBoolean()) throw new CancellationException("Media processing cancelled");
    }

    private static void checkTimeout(long start, long limit, Duration timeout, String binary, Capture stderr) throws MediaExecutionException {
        if (System.nanoTime() - start >= limit) throw new MediaExecutionException(MediaExecutionException.Reason.TIMED_OUT,
                "Media tool timed out after " + timeout + "; binary=" + binary + ", stderr=" + diagnostic(stderr.text()), null, stderr.text(), null);
    }

    private static String diagnostic(String text) {
        return text.replaceAll("[\\p{Cntrl}\\p{Zl}\\p{Zp}]+", " ").strip();
    }

    private static void close(InputStream input) {
        try { input.close(); } catch (IOException ignored) { }
    }

    private static final class Capture {
        private final byte[] bytes;
        private int cursor, size;
        private volatile IOException failure;
        private final Thread thread;

        Capture(InputStream input, int capacity, String name) {
            bytes = new byte[capacity];
            thread = Thread.ofPlatform().daemon().name("teamapps-media-" + name).start(() -> {
                try (input) {
                    byte[] buffer = new byte[8192];
                    for (int n; (n = input.read(buffer)) != -1;) append(buffer, n);
                } catch (IOException error) { failure = error; }
            });
        }

        private synchronized void append(byte[] buffer, int length) {
            for (int i = 0; i < length; i++) {
                bytes[cursor] = buffer[i]; cursor = (cursor + 1) % bytes.length;
            }
            size = Math.min(size + length, bytes.length);
        }

        synchronized String text() {
            byte[] result = new byte[size];
            for (int i = 0; i < size; i++) result[i] = bytes[(cursor - size + i + bytes.length) % bytes.length];
            return new String(result, StandardCharsets.UTF_8);
        }
    }
}
