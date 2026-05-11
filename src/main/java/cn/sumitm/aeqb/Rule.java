package cn.sumitm.aeqb;

/**
 * A single A=B program rule: {@code [key1] pattern = [key2] replacement}.
 * <p>
 * Immutable record with auto-generated equals/hashCode/toString.
 *
 * @param leftRaw          space-stripped left side (for log display)
 * @param rightRaw         space-stripped right side (for log display)
 * @param origLeft         original left side with spaces preserved
 * @param origRight        original right side with spaces preserved
 * @param leftPattern      match pattern (keyword prefix stripped)
 * @param rightReplacement replacement string (keyword prefix stripped)
 */
public record Rule(
        String leftRaw,
        String rightRaw,
        String origLeft,
        String origRight,
        String leftPattern,
        String rightReplacement,
        Keyword key1,
        Keyword key2
) {}
