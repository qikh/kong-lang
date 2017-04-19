package ast

import org.junit.Test
import token.Token
import kotlin.test.assertEquals

class AstTest {

    @Test
    fun astTest() {
        val stmts = listOf(LetStatement(Token(token.LET, "let"),
                                        Identifier(Token(token.IDENT, "myVar"), "myVar"),
                                        Identifier(Token(token.IDENT, "anotherVar"), "anotherVar")))
        val program = Program(stmts)

        assertEquals(program.toString(), "let myVar = anotherVar;")
    }
}
