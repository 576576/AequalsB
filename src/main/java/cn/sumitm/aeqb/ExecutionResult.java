package cn.sumitm.aeqb;

import java.util.List;

/**
 * Result of executing an A=B program on an input string.
 */
public record ExecutionResult(String output, List<StepLog> log, boolean timedOut) {

    /** Return all log lines formatted. */
    public String formatLog() {
        var sb = new StringBuilder();
        for (var step : log) {
            sb.append(step.format());
        }
        return sb.toString();
    }
}
