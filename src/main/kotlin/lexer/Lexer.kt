package lexer

import token.Token
import token.TokenType

val EOF_CHAR = 0.toChar()

class Lexer(val input: String, var position: Int = 0, var readPosition: Int = 0, var ch: Char = EOF_CHAR) {

    init {
        readChar()
    }

    fun readChar() {
        if (readPosition >= input.length) {
            ch = EOF_CHAR
        } else {
            ch = input[readPosition]
        }
        position = readPosition
        readPosition += 1
    }

    fun skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar()
        }
    }

    fun peekChar(): Char {
        if (readPosition >= input.length) {
            return EOF_CHAR
        } else {
            return input[readPosition]
        }
    }

    fun readIdentifier(): String {
        val positionSave = position
        while (isLetter(ch)) {
            readChar()
        }
        return input.substring(positionSave, position)
    }

    fun readNumber(): String {
        val positionSave = position
        while (isDigit(ch)) {
            readChar()
        }
        return input.substring(positionSave, position)
    }

    fun isLetter(c: Char): Boolean {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || ch == '_'
    }

    fun isDigit(c: Char): Boolean {
        return '0' <= c && c <= '9'
    }

    fun readString(): String {
        val positionSave = position + 1
        while (true) {
            readChar()
            if (ch == '"') {
                break
            }
        }
        return input.substring(positionSave, position)
    }

    fun nextToken(): Token {
        var tok: Token

        skipWhitespace()

        when (ch) {
            '=' -> if (peekChar() == '=') {
                val chSave = ch
                readChar()
                tok = Token(token.EQ, String(charArrayOf(chSave, ch)))
            } else {
                tok = newToken(token.ASSIGN, ch)
            }
            '+' -> tok = newToken(token.PLUS, ch)
            '-' -> tok = newToken(token.MINUS, ch)
            '!' ->
                if (peekChar() == '=') {
                    val chSave = ch
                    readChar()
                    tok = Token(token.NOT_EQ, String(charArrayOf(chSave, ch)))
                } else {
                    tok = newToken(token.BANG, ch)
                }
            '/' -> tok = newToken(token.SLASH, ch)
            '*' -> tok = newToken(token.ASTERISK, ch)
            '<' -> tok = newToken(token.LT, ch)
            '>' -> tok = newToken(token.GT, ch)
            ';' -> tok = newToken(token.SEMICOLON, ch)
            ',' -> tok = newToken(token.COMMA, ch)
            '{' -> tok = newToken(token.LBRACE, ch)
            '}' -> tok = newToken(token.RBRACE, ch)
            '(' -> tok = newToken(token.LPAREN, ch)
            ')' -> tok = newToken(token.RPAREN, ch)
            '"' -> tok = Token(token.STRING, readString())
            EOF_CHAR -> tok = Token(token.EOF, "")
            else ->
                if (isLetter(ch)) {
                    val ident = readIdentifier()
                    tok = Token(token.lookupIdent(ident), ident)
                    return tok
                } else if (isDigit(ch)) {
                    tok = Token(token.INT, readNumber())
                    return tok
                } else {
                    tok = newToken(token.ILLEGAL, ch)
                }
        }

        readChar()
        return tok
    }

    fun newToken(tokenType: TokenType, ch: Char): Token {
        return Token(tokenType, ch.toString())
    }
}

