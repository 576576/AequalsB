package cn.sumitm.aeqb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Parser")
class ParserTest {

    @Test
    @DisplayName("parses simple rules")
    void parseSimple() throws Exception {
        var rules = Parser.parse("b=a\naaaa=(return)hello world");
        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).leftPattern()).isEqualTo("b");
        assertThat(rules.get(0).rightReplacement()).isEqualTo("a");
        assertThat(rules.get(0).key1()).isNull();
        assertThat(rules.get(0).key2()).isNull();

        assertThat(rules.get(1).leftPattern()).isEqualTo("aaaa");
        assertThat(rules.get(1).key2()).isEqualTo(Keyword.RETURN);
    }

    @Test
    @DisplayName("parses keyword modifiers")
    void parseWithKeywords() throws Exception {
        var rules = Parser.parse("""
            (start) * =
            (once) xy = ** xy
            appendxy = (end) xy
            """);
        assertThat(rules).hasSize(3);
        assertThat(rules.get(0).key1()).isEqualTo(Keyword.START);
        assertThat(rules.get(1).key1()).isEqualTo(Keyword.ONCE);
        assertThat(rules.get(2).key2()).isEqualTo(Keyword.END);
    }

    @Test
    @DisplayName("removes comment and empty lines")
    void commentsRemoved() throws Exception {
        var rules = Parser.parse("# comment\nb=a\n\n# another\naaaa=(return)hello");
        assertThat(rules).hasSize(2);
    }

    @Test
    @DisplayName("throws on illegal lines")
    void illegalLineThrows() {
        assertThatThrownBy(() -> Parser.parse("a=b=c"))
                .isInstanceOf(Parser.ParseException.class)
                .hasMessageContaining("Not having a single '='");
        assertThatThrownBy(() -> Parser.parse("noequals"))
                .isInstanceOf(Parser.ParseException.class);
    }

    @Test
    @DisplayName("returns empty list for blank input")
    void emptyInput() throws Exception {
        assertThat(Parser.parse("")).isEmpty();
        assertThat(Parser.parse("# just a comment\n")).isEmpty();
    }
}
