package lexer

import org.junit.Test
import kotlin.test.assertEquals


data class Expected(val expectedType: token.TokenType, val expectedLiteral: String)

class LexerTest {

    @Test
    fun lexerTest() {

        val input = """let five = 5;
                    let ten = 10;

                    let add = fn(x, y) {
                        x + y;
                    };

                    let result = add(five, ten);
                    !-/*5;
                    5 < 10 > 5;

                    if (5 < 10) {
                        return true;
                    } else {
                        return false;
                    }

                    10 == 10;
                    10 != 9;
                    "foobar"
                    "foo bar"
                    """

        val tests = arrayOf(
            Expected(token.LET, "let"),
            Expected(token.IDENT, "five"),
            Expected(token.ASSIGN, "="),
            Expected(token.INT, "5"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.LET, "let"),
            Expected(token.IDENT, "ten"),
            Expected(token.ASSIGN, "="),
            Expected(token.INT, "10"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.LET, "let"),
            Expected(token.IDENT, "add"),
            Expected(token.ASSIGN, "="),
            Expected(token.FUNCTION, "fn"),
            Expected(token.LPAREN, "("),
            Expected(token.IDENT, "x"),
            Expected(token.COMMA, ","),
            Expected(token.IDENT, "y"),
            Expected(token.RPAREN, ")"),
            Expected(token.LBRACE, "{"),
            Expected(token.IDENT, "x"),
            Expected(token.PLUS, "+"),
            Expected(token.IDENT, "y"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.RBRACE, "}"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.LET, "let"),
            Expected(token.IDENT, "result"),
            Expected(token.ASSIGN, "="),
            Expected(token.IDENT, "add"),
            Expected(token.LPAREN, "("),
            Expected(token.IDENT, "five"),
            Expected(token.COMMA, ","),
            Expected(token.IDENT, "ten"),
            Expected(token.RPAREN, ")"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.BANG, "!"),
            Expected(token.MINUS, "-"),
            Expected(token.SLASH, "/"),
            Expected(token.ASTERISK, "*"),
            Expected(token.INT, "5"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.INT, "5"),
            Expected(token.LT, "<"),
            Expected(token.INT, "10"),
            Expected(token.GT, ">"),
            Expected(token.INT, "5"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.IF, "if"),
            Expected(token.LPAREN, "("),
            Expected(token.INT, "5"),
            Expected(token.LT, "<"),
            Expected(token.INT, "10"),
            Expected(token.RPAREN, ")"),
            Expected(token.LBRACE, "{"),
            Expected(token.RETURN, "return"),
            Expected(token.TRUE, "true"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.RBRACE, "}"),
            Expected(token.ELSE, "else"),
            Expected(token.LBRACE, "{"),
            Expected(token.RETURN, "return"),
            Expected(token.FALSE, "false"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.RBRACE, "}"),
            Expected(token.INT, "10"),
            Expected(token.EQ, "=="),
            Expected(token.INT, "10"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.INT, "10"),
            Expected(token.NOT_EQ, "!="),
            Expected(token.INT, "9"),
            Expected(token.SEMICOLON, ";"),
            Expected(token.STRING, "foobar"),
            Expected(token.STRING, "foo bar"),
            Expected(token.EOF, "")
        )

        val lexer = Lexer(input)

        tests.forEach {
            val token = lexer.nextToken()
            assertEquals(token.type, it.expectedType)
            assertEquals(token.literal, it.expectedLiteral)
        }
    }
}
