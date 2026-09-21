package org.teamapps.media.exec;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class MediaCommandExecutorTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    private List<String> command(String... args) {
        boolean windows = System.getProperty("os.name").startsWith("Windows");
        List<String> result = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java").toString(),
                "-cp", System.getProperty("java.class.path"), Child.class.getName()));
        result.addAll(List.of(args));
        return result;
    }

    @Test public void argumentsWithSpacesQuotesAndShellCharactersRemainLiteralAndStdinIsClosed() throws Exception {
        String literal = "a path / with spaces ' \" $() ; & ä";
        var result = MediaCommandExecutor.execute(command("echo", literal), Duration.ofSeconds(10));
        assertEquals(literal, result.stdout());
        assertEquals(0, result.exitCode());
        assertTrue(result.elapsed().toNanos() > 0);
    }

    @Test public void bothStreamsAreDrainedAndTailsRemainBounded() throws Exception {
        var result = MediaCommandExecutor.execute(command("noisy"), Duration.ofSeconds(10));
        assertEquals(65536, result.stdout().getBytes(StandardCharsets.UTF_8).length);
        assertEquals(4096, result.stderr().getBytes(StandardCharsets.UTF_8).length);
        assertTrue(result.stdout().endsWith("stdout-end"));
        assertTrue(result.stderr().endsWith("stderr-end"));
    }

    @Test public void redirectedStdoutIsCompleteAndTruncatesExistingFile() throws Exception {
        Path output = temporary.newFile("output with spaces.json").toPath();
        Files.writeString(output, "old trailing data".repeat(100000));
        var result = MediaCommandExecutor.execute(command("noisy"), output, Duration.ofSeconds(10), () -> false);
        String full = Files.readString(output);
        assertEquals(128 * 8192 + 10, full.length());
        assertTrue(full.endsWith("stdout-end"));
        assertFalse(full.contains("old trailing data"));
        assertEquals("", result.stdout());
    }

    @Test public void nonzeroExitCarriesFinalDiagnosticsButNotCommandArguments() {
        var failure = assertThrows(MediaExecutionException.class,
                () -> MediaCommandExecutor.execute(command("fail", "private-argument"), Duration.ofSeconds(10)));
        assertEquals(MediaExecutionException.Reason.NON_ZERO_EXIT, failure.reason());
        assertEquals(Integer.valueOf(7), failure.exitCode());
        assertEquals("codec-not-found", failure.stderr());
        assertFalse(failure.getMessage().contains("private-argument"));
    }

    @Test public void startFailureIsActionable() {
        var failure = assertThrows(MediaExecutionException.class,
                () -> MediaCommandExecutor.execute(List.of(temporary.getRoot().toPath().resolve("missing-tool").toString()), Duration.ofSeconds(1)));
        assertEquals(MediaExecutionException.Reason.START_FAILED, failure.reason());
        assertTrue(failure.getMessage().contains("noexec"));
        assertNotNull(failure.getCause());
    }

    @Test public void timeoutTerminatesRunningProcess() throws Exception {
        Path pid = temporary.getRoot().toPath().resolve("timeout.pid");
        var failure = assertThrows(MediaExecutionException.class,
                () -> MediaCommandExecutor.execute(command("wait", pid.toString()), Duration.ofSeconds(2)));
        assertEquals(MediaExecutionException.Reason.TIMED_OUT, failure.reason());
        assertStopped(pid);
    }

    @Test public void cancellationTerminatesRunningProcess() throws Exception {
        Path pid = temporary.getRoot().toPath().resolve("cancel.pid");
        AtomicBoolean cancel = new AtomicBoolean();
        AtomicReference<Throwable> result = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try { MediaCommandExecutor.execute(command("wait", pid.toString()), null, Duration.ofSeconds(15), cancel::get); }
            catch (Throwable failure) { result.set(failure); }
        });
        worker.start();
        try { awaitStarted(pid); cancel.set(true); worker.join(5000); assertFalse(worker.isAlive()); }
        finally { cancel.set(true); worker.interrupt(); worker.join(5000); }
        assertTrue(String.valueOf(result.get()), result.get() instanceof CancellationException);
        assertStopped(pid);
    }

    @Test public void interruptionTerminatesRunningProcessAndRestoresFlag() throws Exception {
        Path pid = temporary.getRoot().toPath().resolve("interrupt.pid");
        AtomicReference<Throwable> result = new AtomicReference<>();
        AtomicBoolean flag = new AtomicBoolean();
        Thread worker = new Thread(() -> {
            try { MediaCommandExecutor.execute(command("wait", pid.toString()), Duration.ofSeconds(15)); }
            catch (Throwable failure) { result.set(failure); flag.set(Thread.currentThread().isInterrupted()); }
        });
        worker.start();
        try { awaitStarted(pid); worker.interrupt(); worker.join(5000); assertFalse(worker.isAlive()); }
        finally { worker.interrupt(); worker.join(5000); }
        assertTrue(String.valueOf(result.get()), result.get() instanceof InterruptedException);
        assertTrue(flag.get());
        assertStopped(pid);
    }

    @Test public void cancellationBeforeStartCreatesNoOutputFile() {
        Path output = temporary.getRoot().toPath().resolve("not-created");
        assertThrows(CancellationException.class, () -> MediaCommandExecutor.execute(command("fail"), output, Duration.ofSeconds(1), () -> true));
        assertFalse(Files.exists(output));
    }

    @Test public void legacyExitCodeContractRemainsUnchanged() {
        List<String> command = command("fail");
        CommandLineExecutor legacy = new CommandLineExecutor(command.getFirst());
        // Historical behavior intentionally retained; callers opt into the new API for exit-code checking.
        assertTrue(legacy.executeCommand(10, false, command.subList(1, command.size()).toArray(String[]::new)));
    }

    private void awaitStarted(Path pid) throws Exception {
        long start = System.nanoTime();
        while (!Files.exists(pid) || Files.size(pid) == 0) {
            if (System.nanoTime() - start > TimeUnit.SECONDS.toNanos(5)) fail("Child did not start");
            Thread.sleep(10);
        }
    }

    private void assertStopped(Path pid) throws IOException {
        assertTrue("Child must have run", Files.isRegularFile(pid));
        long id = Long.parseLong(Files.readString(pid));
        assertFalse(ProcessHandle.of(id).map(ProcessHandle::isAlive).orElse(false));
    }

    public static class Child {
        public static void main(String[] args) throws Exception {
            switch (args[0]) {
                case "echo" -> { if (System.in.read() != -1) System.exit(9); System.out.print(args[1]); }
                case "noisy" -> {
                    byte[] data = "x".repeat(8192).getBytes(StandardCharsets.UTF_8);
                    for (int i = 0; i < 128; i++) { System.out.write(data); System.err.write(data); }
                    System.out.print("stdout-end"); System.err.print("stderr-end");
                }
                case "fail" -> { System.err.print("codec-not-found"); System.exit(7); }
                case "wait" -> { Files.writeString(Path.of(args[1]), Long.toString(ProcessHandle.current().pid())); Thread.sleep(60000); }
                default -> throw new IllegalArgumentException(args[0]);
            }
        }
    }
}
