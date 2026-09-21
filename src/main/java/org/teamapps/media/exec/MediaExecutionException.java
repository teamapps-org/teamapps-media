package org.teamapps.media.exec;

import java.io.IOException;

/** Structured failure from the opt-in {@link MediaCommandExecutor}. Arguments are not included. */
public final class MediaExecutionException extends IOException {
    public enum Reason { START_FAILED, NON_ZERO_EXIT, TIMED_OUT, OUTPUT_FAILED }

    private final Reason reason;
    private final Integer exitCode;
    private final String stderr;

    MediaExecutionException(Reason reason, String message, Integer exitCode, String stderr, Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.exitCode = exitCode;
        this.stderr = stderr;
    }

    public Reason reason() { return reason; }
    /** Null if the process did not finish with a known exit code. */
    public Integer exitCode() { return exitCode; }
    /** Last 4096 bytes, decoded as UTF-8. May contain paths emitted by the executable. */
    public String stderr() { return stderr; }
}
