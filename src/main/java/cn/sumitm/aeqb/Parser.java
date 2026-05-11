package cn.sumitm.aeqb;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses A=B program source code into a list of {@link Rule} objects.
 */
public final class Parser {
    private Parser() {}

    /**
     * Parse raw program text.
     *
     * @param code the full A=B source code
     * @return an immutable list of parsed rules
     * @throws ParseException if any line is illegal
     */
    public static List<Rule> parse(String code) throws ParseException {
        // Remove comments from the original code (spaces intact)
        var origLines = removeComments(code.lines().toList());
        if (origLines.isEmpty()) return List.of();

        var rules = new ArrayList<Rule>();

        for (int i = 0; i < origLines.size(); i++) {
            int lineNo = i + 1;
            String origLine = origLines.get(i);
            String line = origLine.replace(" ", ""); // space-stripped for pattern matching

            if (Utils.isIllegalProgramLine(line)) {
                throw new ParseException(lineNo, line, "Not having a single '='.");
            }

            int splitIdx = line.indexOf('=');
            String leftExpr  = line.substring(0, splitIdx);
            String rightExpr = line.substring(splitIdx + 1);

            if (Utils.isIllegalProgramExpr(leftExpr, rightExpr)) {
                throw new ParseException(lineNo, line, "Unsupported statement.");
            }

            String leftPattern      = Utils.trimKey(leftExpr);
            String rightReplacement = Utils.trimKey(rightExpr);
            Keyword key1 = keywordOrNull(Utils.getKey(leftExpr));
            Keyword key2 = keywordOrNull(Utils.getKey(rightExpr));

            // Preserve spaces in replacement text only for (return) keyword
            if (key2 == Keyword.RETURN) {
                int origSplit = origLine.indexOf('=');
                rightReplacement = Utils.trimKey(origLine.substring(origSplit + 1));
            }

            // Original left/right with spaces preserved (for display)
            int origSplit = origLine.indexOf('=');
            String origLeft  = origLine.substring(0, origSplit);
            String origRight = origLine.substring(origSplit + 1);

            rules.add(new Rule(leftExpr, rightExpr, origLeft, origRight,
                    leftPattern, rightReplacement, key1, key2));
        }
        return List.copyOf(rules);
    }

    private static Keyword keywordOrNull(String s) {
        if (s == null) return null;
        try { return Keyword.from(s); }
        catch (IllegalArgumentException e) { return null; }
    }

    /** Remove comment lines (starting with {@code #}) and empty lines. */
    private static List<String> removeComments(List<String> lines) {
        var result = new ArrayList<String>();
        for (String line : lines) {
            if (line.isEmpty()) continue;
            int hash = line.indexOf('#');
            if (hash == 0) continue;
            result.add(hash > 0 ? line.substring(0, hash) : line);
        }
        return result;
    }

    /**
     * Format the program source for display with line numbers.
     * Comment lines get {@code -} as line number; empty lines are skipped.
     * Spaces are preserved.
     */
    public static String formatRules(String rawCode, List<Rule> rules) {
        var sb = new StringBuilder();
        var rawLines = rawCode.lines().toList();
        int ruleIdx = 0;

        for (String rawLine : rawLines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                sb.append("-  ").append(rawLine).append('\n');
                continue;
            }

            // Remove inline comment for the check, but keep original for display
            String effective = rawLine;
            int hash = rawLine.indexOf('#');
            if (hash > 0) effective = rawLine.substring(0, hash);

            if (effective.replace(" ", "").isEmpty()) {
                sb.append("-  ").append(rawLine).append('\n');
                continue;
            }

            if (ruleIdx < rules.size()) {
                var r = rules.get(ruleIdx++);
                sb.append("%-2d %s=%s\n".formatted(ruleIdx, r.origLeft(), r.origRight()));
            }
        }
        return sb.toString();
    }

    // ---- exception ----

    /** Thrown when a program line is illegal. Carries line context for error reporting. */
    public static final class ParseException extends Exception {
        public final int line;
        public final String lineContent;

        ParseException(int line, String content, String reason) {
            super("Line " + line + ": " + reason);
            this.line = line;
            this.lineContent = content;
        }
    }
}
