package parser

import ast.*
import lexer.Lexer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ParserTest {

    @Test
    fun testLetStatement() {
        val input = """
        let x = 5;
        let y = 10;
        let foobar = 838383;
        """

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 3)

        val expectedIdentifiers = listOf("x", "y", "foobar")

        for (i in 0..expectedIdentifiers.size - 1) {
            val stmt = program.statements[i]
            val name = expectedIdentifiers[i]

            assertTrue(testLetStatement(stmt, name))
        }
    }

    fun testLetStatement(stmt: Statement, name: String): Boolean {
        if (stmt.tokenLiteral() != "let") {
            println("stmt.TokenLiteral is not 'let', got ${stmt.tokenLiteral()}")
            return false
        }

        if (stmt !is LetStatement) {
            println("stmt is not LetStatement, got ${stmt::class}")
            return false
        }

        if (stmt is LetStatement) {
            if (stmt.name?.value != name) {
                println("letStmt.name.value is not $name, got ${stmt.name?.value}")
                return false
            }

            if (stmt.name?.tokenLiteral() != name) {
                println("stmt.name is not $name, got ${stmt.name}")
                return false
            }
        }

        return true
    }

    @Test
    fun testReturnStatement() {
        val input = """
        return 5;
        return 10;
        return 993322;
        """

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 3)

        for (i in 0..program.statements.size - 1) {
            val stmt = program.statements[i]

            assertTrue(stmt is ReturnStatement)
        }
    }

    @Test
    fun testIdentifierExpression() {
        val input = "foobar;"

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 1)

        val stmt = program.statements[0]
        assert(stmt is ExpressionStatement)

        if (stmt is ExpressionStatement) {
            assert(stmt.expression is Identifier)

            val expression = stmt.expression
            if (expression is Identifier) {
                assertEquals(expression.value, "foobar")
                assertEquals(expression.tokenLiteral(), "foobar")
            }
        }
    }

    @Test
    fun testIntegerExpression() {
        val input = "5;"

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 1)

        val stmt = program.statements[0]
        assert(stmt is ExpressionStatement)

        if (stmt is ExpressionStatement) {
            assert(stmt.expression is IntegerLiteral)

            val exp = stmt.expression
            if (exp is IntegerLiteral) {
                assertEquals(exp.value, 5)
                assertEquals(exp.tokenLiteral(), "5")
            }
        }
    }

    fun testIntegerLiteral(exp: Expression, value: Int): Boolean {
        if (exp is IntegerLiteral) {
            assertEquals(exp.value, value)
            assertEquals(exp.tokenLiteral(), "$value")
        }
        return true
    }

    fun testLongLiteral(exp: Expression, value: Long): Boolean {
        if (exp is LongLiteral) {
            assertEquals(exp.value, value)
            assertEquals(exp.tokenLiteral(), "$value")
        }
        return true
    }

    fun testBooleanLiteral(exp: Expression, value: Boolean): Boolean {
        if (exp is BooleanLiteral) {
            assertEquals(exp.value, value)
            assertEquals(exp.tokenLiteral(), "$value")
        }
        return true
    }


    data class PrefixTest(val input: String, val operator: String, val intVal: Any)

    @Test
    fun testPrefixExpression() {
        val tests = listOf<PrefixTest>(
            PrefixTest("!5", "!", 5),
            PrefixTest("-25", "-", 25),
            PrefixTest("!true", "!", true),
            PrefixTest("!false", "!", false)
        )

        for ((input, operator, intVal) in tests) {

            val lexer = Lexer(input)
            val p = Parser(lexer)

            val program = p.parseProgram()

            if (p.errors.size > 0) {
                println("parser has ${p.errors.size} errors")
                p.errors.forEach { println("parser error $it") }
                assert(p.errors.size > 0)
            }

            assertNotNull(program)
            assertEquals(program.statements.size, 1)

            val stmt = program.statements[0]
            assert(stmt is ExpressionStatement)

            if (stmt is ExpressionStatement) {
                assert(stmt.expression is PrefixExpression)

                val exp = stmt.expression
                if (exp is PrefixExpression) {
                    assertEquals(exp.operator, operator)
                    assert(testLiteralExpression(exp.right, intVal))
                }
            }
        }
    }

    data class InfixTest(val input: String, val leftValue: Any, val operator: String, val rightValue: Any)

    @Test
    fun testInfixExpression() {
        val tests = listOf<InfixTest>(
            InfixTest("5 + 5", 5, "+", 5),
            InfixTest("5 - 5", 5, "-", 5),
            InfixTest("5 * 5", 5, "*", 5),
            InfixTest("5 / 5", 5, "/", 5),
            InfixTest("5 > 5", 5, ">", 5),
            InfixTest("5 < 5", 5, "<", 5),
            InfixTest("5 == 5", 5, "==", 5),
            InfixTest("5 != 5", 5, "!=", 5),
            InfixTest("true == true", true, "==", true),
            InfixTest("false == false", false, "==", false),
            InfixTest("true != false", true, "!=", false)
        )

        for ((input, leftValue, operator, rightValue) in tests) {

            val lexer = Lexer(input)
            val p = Parser(lexer)

            val program = p.parseProgram()

            if (p.errors.size > 0) {
                println("parser has ${p.errors.size} errors")
                p.errors.forEach { println("parser error $it") }
                assert(p.errors.size > 0)
            }

            assertNotNull(program)
            assertEquals(program.statements.size, 1)

            val stmt = program.statements[0]
            assert(stmt is ExpressionStatement)

            if (stmt is ExpressionStatement) {
                assert(stmt.expression is InfixExpression)

                val exp = stmt.expression
                if (exp is InfixExpression) {
                    assert(testLiteralExpression(exp.left, leftValue))
                    assertEquals(exp.operator, operator)
                    assert(testLiteralExpression(exp.right, rightValue))
                }
            }
        }
    }

    @Test
    fun testOperatorPrecedence() {
        val tests = mapOf(
            "-a * b" to "((-a) * b)",
            "!-a" to "(!(-a))",
            "a + b + c" to "((a + b) + c)",
            "a + b - c" to "((a + b) - c)",
            "a * b * c" to "((a * b) * c)",
            "a * b / c" to "((a * b) / c)",
            "a + b / c" to "(a + (b / c))",
            "a + b * c + d / e - f" to "(((a + (b * c)) + (d / e)) - f)",
            "3 + 4; -5 * 5" to "(3 + 4)((-5) * 5)",
            "5 > 4 == 3 < 4" to "((5 > 4) == (3 < 4))",
            "5 > 4 != 3 < 4" to "((5 > 4) != (3 < 4))",
            "3 + 4 * 5 == 3 * 1 + 4 * 5" to "((3 + (4 * 5)) == ((3 * 1) + (4 * 5)))",
            "true" to "true",
            "false" to "false",
            "3 > 5 == true" to "((3 > 5) == true)",
            "1 + (2 + 3) + 4" to "((1 + (2 + 3)) + 4)",
            "(5 + 5) * 2" to "((5 + 5) * 2)",
            "a + add(b * c) + d" to "((a + add((b * c))) + d)",
            "add(a, b, 1, 2 * 3, 4 + 5, add(6, 7 * 8))" to "add(a, b, 1, (2 * 3), (4 + 5), add(6, (7 * 8)))"
        )

        for ((input, expected) in tests) {

            val lexer = Lexer(input)
            val p = Parser(lexer)

            val program = p.parseProgram()

            if (p.errors.size > 0) {
                println("parser has ${p.errors.size} errors")
                p.errors.forEach { println("parser error $it") }
                assert(p.errors.size > 0)
            }

            assertNotNull(program)
            assertEquals(program.toString(), expected)
        }
    }

    fun testIdentifier(exp: Expression, value: String): Boolean {
        if (exp !is Identifier) {
            println("exp is not Identifier, got ${exp::class}")
            return false
        }

        val ident = exp as Identifier
        if (ident.value != value) {
            println("ident.value is not $value, got ${ident.value}")
            return false
        }

        if (ident.tokenLiteral() != value) {
            println("ident.tokenLiteral is not $value, got ${ident.tokenLiteral()}")
            return false
        }

        return true

    }

    fun testLiteralExpression(exp: Expression, expected: Any): Boolean {
        when (expected) {
            is Int -> return testIntegerLiteral(exp, expected)
            is Long -> return testLongLiteral(exp, expected)
            is String -> return testIdentifier(exp, expected)
            is Boolean -> return testBooleanLiteral(exp, expected)
        }

        return false

    }

    fun testInfixExpression(exp: Expression, left: Any, operator: String, right: Any): Boolean {
        if (exp !is InfixExpression) {
            println("exp is not InfixExpression, got ${exp::class}")
            return false
        }

        if (!testLiteralExpression(exp.left, left)) {
            return false
        }

        if (exp.operator != operator) {
            return false
        }

        if (!testLiteralExpression(exp.right, right)) {
            return false
        }

        return true
    }

    @Test
    fun testIfExpression() {
        val input = "if (x<y) { x }"

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 1)

        val stmt = program.statements[0]
        assert(stmt is ExpressionStatement)

        if (stmt is ExpressionStatement) {
            assert(stmt.expression is IfExpression)

            val exp = stmt.expression
            if (exp is IfExpression) {

                assert(testInfixExpression(exp.condition, "x", "<", "y"))

                assert(exp.consequence.statements.size == 1)

                assert(exp.consequence.statements[0] is ExpressionStatement)

                val consequence = exp.consequence.statements[0] as ExpressionStatement

                assert(testIdentifier(consequence.expression!!, "x"))
            }
        }

    }

    @Test
    fun testFunctionLiteral() {
        val input = "fn(x,y) { x + y; }"

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 1)

        val stmt = program.statements[0]
        assert(stmt is ExpressionStatement)

        if (stmt is ExpressionStatement) {
            assert(stmt.expression is FunctionLiteral)

            val exp = stmt.expression
            if (exp is FunctionLiteral) {

                assertEquals(exp.parameters.size, 2)

                testLiteralExpression(exp.parameters[0], "x")
                testLiteralExpression(exp.parameters[1], "y")

                assertEquals(exp.body.statements.size, 1)

                val bodyStmt = exp.body.statements[0]
                assert(bodyStmt is ExpressionStatement)

                if (bodyStmt is ExpressionStatement) {
                    testInfixExpression(bodyStmt.expression!!, "x", "+", "y")
                }
            }
        }

    }

    @Test
    fun testFunctionParameters() {
        val tests = mapOf(
            "fn() {}" to arrayOf<String>(),
            "fn(x) {}" to arrayOf("x"),
            "fn(x,y,z) {}" to arrayOf("x", "y", "z")
        )

        for ((input, expectedParams) in tests) {

            val lexer = Lexer(input)
            val p = Parser(lexer)

            val program = p.parseProgram()

            if (p.errors.size > 0) {
                println("parser has ${p.errors.size} errors")
                p.errors.forEach { println("parser error $it") }
                assert(p.errors.size > 0)
            }

            assertNotNull(program)

            val stmt = program.statements[0] as ExpressionStatement
            val function = stmt.expression as FunctionLiteral

            assert(function.parameters.size == expectedParams.size)

            for (i in 0..function.parameters.size - 1) {
                testLiteralExpression(function.parameters[i], expectedParams[i])
            }
        }

    }

    @Test
    fun testCallExpression() {
        val input = "add(1, 2 * 3, 4 + 5);"

        val lexer = Lexer(input)
        val p = Parser(lexer)

        val program = p.parseProgram()

        if (p.errors.size > 0) {
            println("parser has ${p.errors.size} errors")
            p.errors.forEach { println("parser error $it") }
            assert(p.errors.size > 0)
        }

        assertNotNull(program)
        assertEquals(program.statements.size, 1)

        val stmt = program.statements[0]
        assert(stmt is ExpressionStatement)

        if (stmt is ExpressionStatement) {
            assert(stmt.expression is CallExpression)

            val exp = stmt.expression
            if (exp is CallExpression) {

                assert(testIdentifier(exp.function, "add"))

                testLiteralExpression(exp.arguments[0], 1)
                testInfixExpression(exp.arguments[1], 2, "*", 3)
                testInfixExpression(exp.arguments[2], 4, "+", 5)
            }
        }

    }

}
