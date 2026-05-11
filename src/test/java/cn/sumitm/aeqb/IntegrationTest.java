package cn.sumitm.aeqb;

import java.nio.file.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests using the sample case files.
 */
@DisplayName("Integration — sample cases")
class IntegrationTest {

    private static Path sampleDir() {
        return Path.of("sample_cases");
    }

    private static String readSample(String filename) throws Exception {
        return Files.readString(sampleDir().resolve(filename));
    }

    private static String run(String program, String input) throws Exception {
        var rules = Parser.parse(program);
        return new Engine(rules, 1000).execute(input).output();
    }

    @Test
    @DisplayName("code1 — simple replace, all cases")
    void code1AllCases() throws Exception {
        var program = readSample("code1.txt");
        var ioLines = readSample("code1_io.txt").lines().toList();
        assertThat(ioLines).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ioLines.size() % 2).isZero();

        for (int i = 0; i < ioLines.size(); i += 2) {
            String input    = ioLines.get(i);
            String expected = ioLines.get(i + 1);
            assertThat(run(program, input))
                    .as("Test case %d: input='%s'".formatted(i / 2, input))
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("code2 — binary multiplication, all cases")
    void code2AllCases() throws Exception {
        var program = readSample("code2.txt");
        var ioLines = readSample("code2_io.txt").lines().toList();
        assertThat(ioLines).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ioLines.size() % 2).isZero();

        for (int i = 0; i < ioLines.size(); i += 2) {
            String input    = ioLines.get(i);
            String expected = ioLines.get(i + 1);
            assertThat(run(program, input))
                    .as("Test case %d: input='%s'".formatted(i / 2, input))
                    .isEqualTo(expected);
        }
    }

    // ---- Individual code1 cases ----

    @Test @DisplayName("code1: aaabbababa → hello world")
    void code1_case0() throws Exception {
        assertThat(run(readSample("code1.txt"), "aaabbababa"))
                .isEqualTo("hello world");
    }

    @Test @DisplayName("code1: aba → sayonara")
    void code1_case1() throws Exception {
        assertThat(run(readSample("code1.txt"), "aba"))
                .isEqualTo("sayonara");
    }

    @Test @DisplayName("code1: aaaa → hello world")
    void code1_case3() throws Exception {
        assertThat(run(readSample("code1.txt"), "aaaa"))
                .isEqualTo("hello world");
    }

    // ---- Individual code2 cases ----

    @Test @DisplayName("code2: 11*1 → 11 (3×1=3)")
    void code2_case0() throws Exception {
        assertThat(run(readSample("code2.txt"), "11*1"))
                .isEqualTo("11");
    }

    @Test @DisplayName("code2: 101*10 → 1010 (5×2=10)")
    void code2_case1() throws Exception {
        assertThat(run(readSample("code2.txt"), "101*10"))
                .isEqualTo("1010");
    }

    @Test @DisplayName("code2: 11*11 → 1001 (3×3=9)")
    void code2_case2() throws Exception {
        assertThat(run(readSample("code2.txt"), "11*11"))
                .isEqualTo("1001");
    }
}
