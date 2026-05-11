package cn.sumitm.aeqb;

/**
 * One execution step in the detailed log.
 */
public record StepLog(int step, int line, String left, String right, String result) {

    /** Format as fixed-width columns with result aligned via padding. */
    public String format() {
        String expr = "%s=%s".formatted(left, right);
        return "%-2d %-2d %-30s %s\n".formatted(step, line, expr, result);
    }
}
