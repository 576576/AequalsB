package cn.sumitm.aeqb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Engine")
class EngineTest {

    private static String run(String program, String input, int timeLimit) throws Exception {
        var rules = Parser.parse(program);
        return new Engine(rules, timeLimit).execute(input).output();
    }

    private static String run(String program, String input) throws Exception {
        return run(program, input, 1000);
    }

    @Test
    @DisplayName("returns input unchanged when no rule matches")
    void noMatchReturnsInput() throws Exception {
        assertThat(run("x=y\n", "hello")).isEqualTo("hello");
    }

    @Test
    @DisplayName("simple replace: b → a")
    void simpleReplace() throws Exception {
        assertThat(run("b=a\n", "bbb")).isEqualTo("aaa");
    }

    @Test
    @DisplayName("(once) fires only once")
    void onceKeyword() throws Exception {
        assertThat(run("(once)a=b\nb=c\n", "aaa")).isEqualTo("caa");
    }

    @Test
    @DisplayName("(start) matches only at beginning")
    void startKeyword() throws Exception {
        assertThat(run("(start)ab=XY\na=X\n", "abcab")).isEqualTo("XYcXb");
    }

    @Test
    @DisplayName("(end) matches only at end")
    void endKeyword() throws Exception {
        assertThat(run("(end)ab=XY\nb=Z\n", "cabab")).isEqualTo("caZXY");
    }

    @Test
    @DisplayName("(return) stops execution immediately")
    void returnKeyword() throws Exception {
        assertThat(run("a=(return)X\nb=Y\n", "ab")).isEqualTo("X");
    }

    @Test
    @DisplayName("empty pattern with no anchor loops until timeout")
    void emptyPatternLoops() throws Exception {
        var rules = Parser.parse("=X\n");
        var result = new Engine(rules, 50).execute("abc");
        assertThat(result.timedOut()).isTrue();
        assertThat(result.output()).startsWith("X");
    }

    @Test
    @DisplayName("timedOut flag set when limit exceeded")
    void timeLimitExceeded() throws Exception {
        var rules = Parser.parse("a=a\n");
        var result = new Engine(rules, 10).execute("aaa");
        assertThat(result.timedOut()).isTrue();
        assertThat(result.output()).isNotEmpty();
    }

    @Test
    @DisplayName("records a detailed step log")
    void recordsStepLog() throws Exception {
        var rules = Parser.parse("b=a\na=(return)X\n");
        var result = new Engine(rules, 1000).execute("ab");
        assertThat(result.log()).isNotEmpty();
        // verify format
        for (var step : result.log()) {
            assertThat(step.format()).containsPattern("^\\d+\\s+\\d+\\s+.*=.*\\s{2,}.*");
        }
    }
}
