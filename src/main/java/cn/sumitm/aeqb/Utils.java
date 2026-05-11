package cn.sumitm.aeqb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;

/**
 * Utility functions for bracket matching, keyword extraction, and validation.
 * <p>
 * All methods are static — this is a pure utility class.
 */
public final class Utils {
    private Utils() {}

    /**
     * Find the matching closing bracket {@code )} starting from {@code start}.
     * Uses a stack-based approach that handles nested brackets.
     *
     * @return the index of matching {@code )}, or -1 if none found
     */
    public static int getEndBracket(String expr, int start) {
        var stack = new ArrayDeque<Integer>();
        for (int i = start; i < expr.length(); i++) {
            switch (expr.charAt(i)) {
                case '(' -> stack.push(i);
                case ')' -> {
                    if (stack.isEmpty()) return -1; // mismatched
                    stack.pop();
                    if (stack.isEmpty()) return i;
                }
            }
        }
        return -1;
    }

    /** Extract the keyword inside the first {@code (…)} pair. */
    public static String getKey(String regex) {
        if (!regex.contains("(")) return null;
        int open = regex.indexOf('(');
        int close = regex.indexOf(')');
        return close > open ? regex.substring(open + 1, close) : null;
    }

    /** Strip the {@code (keyword)} prefix from an expression. */
    public static String trimKey(String regex) {
        if (!regex.contains("(")) return regex;
        int close = regex.indexOf(')');
        return close >= 0 ? regex.substring(close + 1).trim() : regex;
    }

    /** A line is illegal if it has zero or more than one '='. */
    public static boolean isIllegalProgramLine(String line) {
        int first = line.indexOf('=');
        return first != line.lastIndexOf('=') || first == -1;
    }

    /** Check if a statement contains unsupported keywords. */
    public static boolean isIllegalStatement(String stmt, String... allowedKeys) {
        stmt = stmt.trim();
        if (stmt.contains("(")) {
            int keyStart = stmt.indexOf('(');
            int keyEnd = getEndBracket(stmt, keyStart);
            if (keyStart == 0 && keyEnd != -1) {
                String key = stmt.substring(keyStart + 1, keyEnd).toLowerCase();
                for (var k : allowedKeys) if (key.equals(k)) return false;
            }
            return true;
        }
        return stmt.contains(")");
    }

    public static boolean isIllegalProgramExpr(String left, String right) {
        return isIllegalStatement(left, "start", "end", "once")
            || isIllegalStatement(right, "start", "end", "return");
    }

    public static boolean isIllegalInput(String s) {
        return (!s.contains("#") && s.contains("="))
            || s.contains("(") || s.contains(")");
    }

    public static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Print an error message in red (ANSI escape). */
    public static void printError(String msg) {
        System.out.println("\033[31m" + msg + "\033[0m");
    }

    // ---- file I/O ----

    public static String readFile(String path) {
        try {
            var p = Path.of(path);
            if (!Files.exists(p)) Files.createFile(p);
            return Files.readString(p);
        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            return "";
        }
    }

    public static void writeFile(String path, String content) {
        try {
            Files.writeString(Path.of(path), content);
        } catch (IOException e) {
            System.err.println("写入文件时出错: " + e.getMessage());
        }
    }
}
