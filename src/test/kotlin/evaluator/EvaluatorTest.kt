package evaluator

import lexer.Lexer
import org.junit.Test
import parser.Parser
import kotlin.test.assertEquals

class EvaluatorTest {
    @Test
    fun testEvalIntegerExpression() {
        val tests = mapOf(
            "5" to 5,
            "10" to 10
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testIntegerObject(evaluated, expected)
        }
    }

    fun testEval(input: String): types.Object {
        val lexer = Lexer(input)
        val p = Parser(lexer)
        val program = p.parseProgram()

        return Eval(program)
    }

    fun testIntegerObject(obj: types.Object, expected: Int) {
        assert(obj is types.Integer)

        if (obj is types.Integer) {
            assertEquals(obj.value, expected)
        }
    }
}
