package cn.sumitm.aeqb;

/**
 * Keyword modifiers in A=B language, appearing in {@code (keyword)} prefix.
 */
public enum Keyword {
    START,
    END,
    ONCE,
    RETURN;

    /**
     * Parse a keyword from its lowercase string representation.
     */
    public static Keyword from(String s) {
        return switch (s.toLowerCase()) {
            case "start"  -> START;
            case "end"    -> END;
            case "once"   -> ONCE;
            case "return" -> RETURN;
            default       -> throw new IllegalArgumentException("Unknown keyword: " + s);
        };
    }
}
