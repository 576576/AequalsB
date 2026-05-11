package cn.sumitm.aeqb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Core A=B execution engine.
 * <p>
 * Fully encapsulated — no static mutable state. Create one instance per execution run.
 * Mirrors the Rust {@code Engine} struct design.
 */
public final class Engine {
    private final List<Rule> rules;
    private final int timeLimit;

    // ---- per-execution mutable state ----
    private final Set<Integer> ignoredLines = new HashSet<>();
    private int lineIndex;
    private int executedLines;
    private String mainString;
    private final List<StepLog> log = new ArrayList<>();
    private boolean isEnded;

    public Engine(List<Rule> rules, int timeLimit) {
        this.rules = List.copyOf(rules);
        this.timeLimit = timeLimit;
    }

    /**
     * Execute the program on the given input string.
     *
     * @return the execution result containing output, step log, and timeout flag
     */
    public ExecutionResult execute(String input) {
        ignoredLines.clear();
        lineIndex = 0;
        executedLines = 0;
        mainString = input;
        log.clear();
        isEnded = false;

        while (!isEnded && lineIndex < rules.size()) {
            if (executedLines > timeLimit) {
                return new ExecutionResult(mainString, List.copyOf(log), true);
            }

            Rule rule = rules.get(lineIndex);
            if (tryMatch(rule)) {
                executedLines++;
                log.add(new StepLog(executedLines, lineIndex + 1,
                        rule.origLeft(), rule.origRight(), mainString));
                if (!isEnded) lineIndex = 0;
            } else {
                lineIndex++;
            }
        }

        return new ExecutionResult(mainString, List.copyOf(log), false);
    }

    // ---- match dispatch ----

    private boolean tryMatch(Rule rule) {
        String pat  = rule.leftPattern();
        String repl = rule.rightReplacement();
        Keyword k1  = rule.key1();

        if (k1 == null || k1 == Keyword.ONCE) return handleNoneOrOnce(rule, pat, repl);
        return switch (k1) {
            case START  -> handleStart(rule, pat, repl);
            case END    -> handleEnd(rule, pat, repl);
            case RETURN -> false;
            case ONCE   -> false; // unreachable, handled above
        };
    }

    private static final Keyword ONCE = Keyword.ONCE;

    // ---- match handlers ----

    private boolean handleNoneOrOnce(Rule rule, String pat, String repl) {
        if (rule.key1() == Keyword.ONCE) {
            if (ignoredLines.contains(lineIndex)) return false;
            ignoredLines.add(lineIndex);
        }
        if (!mainString.contains(pat)) return false;
        applyReplacement(rule.key2(), pat, repl);
        return true;
    }

    private boolean handleStart(Rule rule, String pat, String repl) {
        if (!mainString.startsWith(pat)) return false;
        if (rule.key2() == null) {
            mainString = replaceFirstLiteral(mainString, pat, repl);
        } else {
            mainString = mainString.substring(pat.length());
            mainString = doReplace(pat, repl, rule.key2());
        }
        return true;
    }

    private boolean handleEnd(Rule rule, String pat, String repl) {
        if (!mainString.endsWith(pat)) return false;
        if (rule.key2() == null) {
            int pos = mainString.length() - pat.length();
            mainString = mainString.substring(0, pos) + repl;
        } else {
            mainString = mainString.substring(0, mainString.length() - pat.length());
            mainString = doReplace(pat, repl, rule.key2());
        }
        return true;
    }

    // ---- replacement helpers ----

    private void applyReplacement(Keyword key2, String pat, String repl) {
        if (key2 == null) {
            mainString = replaceFirstLiteral(mainString, pat, repl);
        } else {
            mainString = doReplace(pat, repl, key2);
        }
    }

    private String doReplace(String pat, String repl, Keyword key2) {
        return switch (key2) {
            case START  -> repl + replaceFirstLiteral(mainString, pat, "");
            case END    -> replaceFirstLiteral(mainString, pat, "") + repl;
            case RETURN -> { isEnded = true; yield repl; }
            case ONCE   -> replaceFirstLiteral(mainString, pat, repl);
        };
    }

    /**
     * Replace the first occurrence of a literal string (not a regex).
     * Uses {@link Pattern#quote} to escape regex metacharacters.
     */
    private static String replaceFirstLiteral(String input, String literal, String replacement) {
        return input.replaceFirst(
                Pattern.quote(literal),
                java.util.regex.Matcher.quoteReplacement(replacement));
    }
}
