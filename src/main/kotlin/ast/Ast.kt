package ast

import token.Token

interface Node {
    fun tokenLiteral(): String
}

interface Statement : Node {
    fun statementNode(): Node
}

interface Expression : Node {
    fun expressionNode(): Node
}

class IllegalExpression(val token: Token) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return token.literal
    }

}

class Identifier(val token: Token, val value: String) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return value
    }

}

class IntegerLiteral(val token: Token, val value: Int) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return token.literal
    }

}

class LongLiteral(val token: Token, val value: Long) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return token.literal
    }

}

class StringLiteral(val token: Token, val value: String) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return token.literal
    }

}

class BooleanLiteral(val token: Token, val value: Boolean) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return token.literal
    }

}

class PrefixExpression(val token: Token, val operator: String, val right: Expression) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append("(")
        buffer.append(operator)
        buffer.append(right.toString())
        buffer.append(")")

        return buffer.toString()
    }

}

class InfixExpression(val token: Token, val left: Expression, val operator: String,
                      val right: Expression) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append("(")
        buffer.append(left.toString())
        buffer.append(" $operator ")
        buffer.append(right.toString())
        buffer.append(")")

        return buffer.toString()
    }

}

class Program(var statements: List<out Statement> = listOf()) : Node {
    override fun tokenLiteral(): String {
        if (statements.isNotEmpty()) {
            return statements[0].tokenLiteral()
        } else {
            return ""
        }
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        statements.forEach { buffer.append(it.toString()) }
        return buffer.toString()
    }
}

class LetStatement(var token: Token, var name: Identifier, var value: Expression) : Statement {
    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun statementNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append(tokenLiteral() + " ")
        buffer.append(name)
        buffer.append(" = ")

        if (value != null) {
            buffer.append(value.toString())
        }

        buffer.append(";")
        return buffer.toString()
    }
}

class ReturnStatement(var token: Token, var returnValue: Expression) : Statement {
    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun statementNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append(tokenLiteral() + " ")

        if (returnValue != null) {
            buffer.append(returnValue.toString())
        }

        buffer.append(";")
        return buffer.toString()
    }
}

class ExpressionStatement(var token: Token, var expression: Expression) : Statement {
    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun statementNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append(expression.toString())

        return buffer.toString()
    }
}

class BlockStatement(val token: Token, val statements: MutableList<Statement> = mutableListOf()) : Statement {
    override fun tokenLiteral(): String {
        return this.token.literal
    }

    override fun statementNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        statements.forEach { buffer.append(it.toString()) }

        return buffer.toString()
    }
}

class IfExpression(val token: Token, var condition: Expression, var consequence: BlockStatement,
                   val alternative: BlockStatement?) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append("if")
        buffer.append(condition.toString())
        buffer.append(" ")
        buffer.append(consequence.toString())

        if (alternative != null) {
            buffer.append("else ")
            buffer.append(alternative.toString())
        }

        return buffer.toString()
    }

}

class FunctionLiteral(val token: Token, var parameters: List<Identifier>, var body: BlockStatement) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append(tokenLiteral())
        buffer.append("(")
        buffer.append(parameters.joinToString(","))
        buffer.append(")")
        buffer.append(body.toString())

        return buffer.toString()
    }

}

class CallExpression(val token: Token, var function: Expression, var arguments: List<Expression>) : Expression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    override fun expressionNode(): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        val buffer = StringBuilder()

        buffer.append(function.toString())
        buffer.append("(")
        buffer.append(arguments.joinToString(", "))
        buffer.append(")")

        return buffer.toString()
    }

}
