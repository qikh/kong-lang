package parser

import ast.*
import lexer.Lexer
import token.Token
import token.TokenType

enum class Precedence {
    LOWEST,
    EQUALS, // ==
    LESSGREATER, // > or <
    SUM, // +
    PRODUCT, // *
    PREFIX, // -X or !X
    CALL            // myFunction(X)”
}

val precedences = mapOf(
    token.EQ to Precedence.EQUALS,
    token.NOT_EQ to Precedence.EQUALS,
    token.LT to Precedence.LESSGREATER,
    token.GT to Precedence.LESSGREATER,
    token.PLUS to Precedence.SUM,
    token.MINUS to Precedence.SUM,
    token.SLASH to Precedence.PRODUCT,
    token.ASTERISK to Precedence.PRODUCT,
    token.LPAREN to Precedence.CALL
)

typealias prefixParseFn = () -> Expression
typealias infixParseFn = (Expression) -> Expression

class Parser(val lexer: Lexer) {

    lateinit var curToken: Token
    lateinit var peekToken: Token

    val prefixExpressions = mutableMapOf<TokenType, () -> Expression>()
    val infixExpressions = mutableMapOf<TokenType, (Expression) -> Expression>()

    val errors = mutableListOf<String>()


    val parseIdentifier: () -> Expression = {
        Identifier(curToken, curToken.literal)
    }

    val parseIntegerLiteral: () -> Expression = {
        try {
            val value = curToken.literal.toInt()

            IntegerLiteral(curToken, value)
        } catch(e: NumberFormatException) {
            println("could not parse ${curToken.literal} as integer")
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }
    }

    val parseLongLiteral: () -> Expression = {
        try {
            val value = curToken.literal.toLong()

            LongLiteral(curToken, value)
        } catch(e: NumberFormatException) {
            println("could not parse ${curToken.literal} as integer")
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }
    }

    val parseBoolean: () -> Expression = {
        BooleanLiteral(curToken, curTokenIs(token.TRUE))
    }

    val parsePrefixExpression: () -> Expression = {
        val token = curToken

        nextToken()

        val expression = PrefixExpression(token, token.literal, parseExpression(Precedence.PREFIX))

        expression
    }

    val parseInfixExpression: (Expression) -> Expression = {
        val token = curToken
        val precedence = curPrecedence()

        nextToken()

        val expression = InfixExpression(token, it, token.literal, parseExpression(precedence))

        expression
    }

    val parseGroupedExpression: () -> Expression = {
        nextToken()

        val exp = parseExpression(Precedence.LOWEST)

        if (!expectedPeek(token.RPAREN)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        } else {
            exp
        }
    }

    val parseIfExpression: () -> Expression = {
        val ifToken = curToken

        if (!expectedPeek(token.LPAREN)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        nextToken()

        val condition = parseExpression(Precedence.LOWEST)

        if (!expectedPeek(token.RPAREN)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        if (!expectedPeek(token.LBRACE)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        val consequence = parseBlockStatement()

        if (peekTokenIs(token.ELSE)) {
            nextToken()

            if (!expectedPeek(token.LBRACE)) {
                IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
            }

            val alternative = parseBlockStatement()
            IfExpression(ifToken, condition, consequence, alternative)
        } else {
            IfExpression(ifToken, condition, consequence, null)
        }

    }

    fun parseBlockStatement(): BlockStatement {
        val block = BlockStatement(curToken)

        nextToken()

        while (!curTokenIs(token.RBRACE)) {
            val stmt = parseStatement()

            if (stmt != null) {
                block.statements.add(stmt)
            }
            nextToken()
        }

        return block
    }

    val parseFunctionLiteral: () -> Expression = {
        val theToken = curToken

        if (!expectedPeek(token.LPAREN)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        val params = parseFunctionParameters()

        if (!expectedPeek(token.LBRACE)) {
            IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        val body = parseBlockStatement()

        FunctionLiteral(theToken, params, body)

    }

    fun parseFunctionParameters(): List<Identifier> {
        val identifiers = mutableListOf<Identifier>()

        if (peekTokenIs(token.RPAREN)) {
            nextToken()
            return identifiers
        }

        nextToken()

        val ident = Identifier(curToken, curToken.literal)
        identifiers.add(ident)

        while (peekTokenIs(token.COMMA)) {
            nextToken()
            nextToken()

            identifiers.add(Identifier(curToken, curToken.literal))
        }

        if (!expectedPeek(token.RPAREN)) {
            return listOf()
        }

        return identifiers.toList()
    }

    val parseCallExpression: (Expression) -> Expression = {
        val theToken = curToken
        val arguments = parseCallArguments()

        CallExpression(theToken, it, arguments)
    }

    fun parseCallArguments(): List<Expression> {
        val args = mutableListOf<Expression>()

        if (peekTokenIs(token.RPAREN)) {
            nextToken()
            return args
        }

        nextToken()

        args.add(parseExpression(Precedence.LOWEST))

        while (peekTokenIs(token.COMMA)) {
            nextToken()
            nextToken()

            args.add(parseExpression(Precedence.LOWEST))
        }

        if (!expectedPeek(token.RPAREN)) {
            return listOf()
        }

        return args.toList()
    }

    init {
        curToken = lexer.nextToken()
        peekToken = lexer.nextToken()

        registerPrefix(token.IDENT, parseIdentifier)
        registerPrefix(token.INT, parseIntegerLiteral)
        registerPrefix(token.BANG, parsePrefixExpression)
        registerPrefix(token.MINUS, parsePrefixExpression)
        registerPrefix(token.TRUE, parseBoolean)
        registerPrefix(token.FALSE, parseBoolean)

        registerPrefix(token.LPAREN, parseGroupedExpression)

        registerPrefix(token.IF, parseIfExpression)

        registerPrefix(token.FUNCTION, parseFunctionLiteral)

        registerInfix(token.PLUS, parseInfixExpression)
        registerInfix(token.MINUS, parseInfixExpression)
        registerInfix(token.SLASH, parseInfixExpression)
        registerInfix(token.ASTERISK, parseInfixExpression)
        registerInfix(token.EQ, parseInfixExpression)
        registerInfix(token.NOT_EQ, parseInfixExpression)
        registerInfix(token.LT, parseInfixExpression)
        registerInfix(token.GT, parseInfixExpression)

        registerInfix(token.LPAREN, parseCallExpression)
    }

    fun registerPrefix(tokenType: TokenType, expression: prefixParseFn) {
        prefixExpressions[tokenType] = expression
    }

    fun registerInfix(tokenType: TokenType, expression: infixParseFn) {
        infixExpressions[tokenType] = expression
    }

    fun peekPrecedence(): Precedence {
        val p = precedences[peekToken.type]
        if (p != null) {
            return p
        }
        return Precedence.LOWEST
    }

    fun curPrecedence(): Precedence {
        val p = precedences[curToken.type]
        if (p != null) {
            return p
        }
        return Precedence.LOWEST
    }

    fun nextToken() {
        curToken = peekToken
        peekToken = lexer.nextToken()
    }

    fun parseProgram(): Program {
        val program = Program()
        while (curToken.type != token.EOF) {
            val stmt = parseStatement()

            if (stmt != null) {
                program.statements = program.statements + stmt
            } else {
                return program
            }
            nextToken()
        }
        return program
    }

    fun parseStatement(): Statement? {
        when (curToken.type) {
            token.LET -> return parseLetStatement()
            token.RETURN -> return parseReturnStatement()
            else -> return parseExpressionStatement()
        }
    }

    fun parseLetStatement(): Statement? {
        val stmt = LetStatement(curToken)

        if (!expectedPeek(token.IDENT)) {
            return null
        }

        stmt.name = Identifier(curToken, curToken.literal)

        if (!expectedPeek(token.ASSIGN)) {
            return null
        }

        nextToken()

        stmt.value = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(token.SEMICOLON)) {
            nextToken()
        }

        return stmt
    }

    fun parseReturnStatement(): Statement? {
        val stmt = ReturnStatement(curToken)

        nextToken()

        stmt.returnValue = parseExpression(Precedence.LOWEST)

        if (peekTokenIs(token.SEMICOLON)) {
            nextToken()
        }

        return stmt
    }

    fun parseExpressionStatement(): ExpressionStatement? {
        val expression = parseExpression(Precedence.LOWEST)
        if (expression !is IllegalExpression) {
            val stmt = ExpressionStatement(curToken, expression)
            if (peekTokenIs(token.SEMICOLON)) {
                nextToken()
            }

            return stmt
        } else {
            return null
        }
    }

    fun parseExpression(precedence: Precedence): Expression {
        val prefix = prefixExpressions[curToken.type]

        if (prefix == null) {
            return IllegalExpression(Token(token.ILLEGAL, token.ILLEGAL))
        }

        var leftExp = prefix()

        while (!peekTokenIs(token.SEMICOLON) && precedence < peekPrecedence()) {
            val infix = infixExpressions[peekToken.type]
            if (infix == null) {
                return leftExp
            }

            nextToken()

            leftExp = infix(leftExp)
        }

        return leftExp
    }

    fun curTokenIs(t: TokenType): Boolean {
        return curToken.type == t
    }

    fun peekTokenIs(t: TokenType): Boolean {
        return peekToken.type == t
    }

    fun expectedPeek(t: TokenType): Boolean {
        if (peekTokenIs(t)) {
            nextToken()
            return true
        } else {
            peekError(t)
            return false
        }
    }

    fun peekError(t: TokenType) {
        errors.add("Expected next token to be $t, got ${peekToken.type}")
    }

}
