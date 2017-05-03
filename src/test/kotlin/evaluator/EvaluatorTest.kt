package evaluator

import environment.Environment
import lexer.Lexer
import org.junit.Test
import parser.Parser
import types.Error
import types.NULL
import types.Object
import kotlin.test.assertEquals

class EvaluatorTest {
    @Test
    fun testEvalIntegerExpression() {
        val tests = mapOf(
            "5" to 5,
            "10" to 10,
            "-5" to -5,
            "-10" to -10,
            "5 + 5 + 5 + 5 - 10" to 10,
            "2 * 2 * 2 * 2 * 2" to 32,
            "-50 + 100 + -50" to 0,
            "5 * 2 + 10" to 20,
            "5 + 2 * 10" to 25,
            "20 + 2 * -10" to 0,
            "50 / 2 * 2 + 10" to 60,
            "(5 + 10 * 2 + 15 / 3) * 2 - 10" to 50
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
        val env = Environment()

        return Eval(program, env)
    }

    fun testIntegerObject(obj: types.Object, expected: Int) {
        assert(obj is types.Integer)

        if (obj is types.Integer) {
            assertEquals(obj.value, expected)
        }
    }

    @Test
    fun testEvalBooleanExpression() {
        val tests = mapOf(
            "true" to true,
            "false" to false,
            "1 < 2" to true,
            "1 > 2" to false,
            "1 == 1" to true,
            "1 != 1" to false,
            "1 == 2" to false,
            "(1 < 2) == true" to true,
            "(1 < 2) == false" to false
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testBooleanObject(evaluated, expected)
        }
    }

    fun testBooleanObject(obj: types.Object, expected: Boolean) {
        assert(obj is types.Boolean)

        if (obj is types.Boolean) {
            assertEquals(obj.value, expected)
        }
    }

    @Test
    fun testEvalBangOperator() {
        val tests = mapOf(
            "!true" to false,
            "!false" to true,
            "!!true" to true,
            "!!false" to false,
            "!!5" to true
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            testBooleanObject(evaluated, expected)
        }
    }

    @Test
    fun testEvalIfElseExpression() {
        val tests = mapOf(
            "if (true) {10}" to 10,
            "if (false) {10}" to null
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            if (expected is Int) {
                testIntegerObject(evaluated, expected)
            } else {
                testNullObject(evaluated)
            }
        }
    }

    private fun testNullObject(evaluated: Object) {
        assert(evaluated == NULL)
    }

    @Test
    fun testEvalReturnStatement() {
        val tests = mapOf(
            "return 10" to 10,
            "return 10; 9;" to 10,
            "return 2 * 5; 9;" to 10,
            """ if (10 > 1) {
                  if (10 > 1) {
                    return 10;
                  }

                  return 1;
                }
            """ to 10
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)
            if (expected is Int) {
                testIntegerObject(evaluated, expected)
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun testErrorHandling() {
        val tests = mapOf(
            "5 + true" to "type mismatch: INTEGER + BOOL",
            "5 + true; 5;" to "type mismatch: INTEGER + BOOL",
            "-true" to "unknown operator: -BOOL",
            "true + false" to "unknown operator: BOOL + BOOL",
            "5; true + false; 5" to "unknown operator: BOOL + BOOL",
            "if(10+1){true + false;}" to "unknown operator: BOOL + BOOL",
            """ if (10 > 1) {
                  if (10 > 1) {
                    return true + false;
                  }

                  return 1;
                }
            """ to "unknown operator: BOOL + BOOL",
            "foobar" to "identifier not found: foobar"
        )

        for ((input, expectedMessage) in tests) {
            val evaluated = testEval(input)

            assert(evaluated is types.Error)

            if (evaluated is types.Error) {
                assertEquals(evaluated.messge, expectedMessage)
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun testLetStatements() {
        val tests = mapOf(
            "let a = 5; a" to 5,
            "let a = 5 * 5; a;" to 25,
            "let a = 5; let b = a; b;" to 5,
            "let a = 5; let b = a; let c = a + b + 5; c;" to 15
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)

            assert(evaluated is types.Integer)

            if (evaluated is types.Integer) {
                testIntegerObject(evaluated, expected)
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun testFunctionApplication() {
        val tests = mapOf(
            "let identity = fn(x){x;} identity(5);" to 5,
            "let identity = fn(x){return x;} identity(5);" to 5,
            "let dbl = fn(x){x * 2;} dbl(5);" to 10,
            "let add = fn(x, y){x + y;} add(5, 5);" to 10,
            "let add = fn(x, y){x + y;} add(5 + 5, add(5,5));" to 20,
            "fn(x){x;} (5)" to 5
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)

            assert(evaluated is types.Integer)

            if (evaluated is types.Integer) {
                testIntegerObject(evaluated, expected)
            } else {
                testNullObject(evaluated)
            }
        }
    }

    @Test
    fun testStringLiteral() {
        val input = """
                    "Hello world"
                    """

        val evaluated = testEval(input)

        assert(evaluated is types.Str)

        if (evaluated is types.Str) {
            assert(evaluated.value == "Hello world")
        }
    }

    @Test
    fun testBuiltinFunction() {
        val tests = mapOf(
            "len(\"\")" to 0,
            "len(\"four\")" to 4,
            "len(1)" to "argument to `len` not supported, got INTEGER",
            "len(\"one\",\"two\")" to "wrong number of arguments."
        )

        for ((input, expected) in tests) {
            val evaluated = testEval(input)

            if (evaluated is types.Integer && expected is Int) {
                testIntegerObject(evaluated, expected)
            } else if (evaluated is Error && expected is String) {
                assertEquals(evaluated.messge, expected)
            } else {
                testNullObject(evaluated)
            }
        }
    }
}
