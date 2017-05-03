package token

typealias TokenType = String

val ILLEGAL: TokenType = "ILLEGAL"
val EOF: TokenType = "EOF"

// Identifiers + literals
val IDENT: TokenType = "IDENT" // add, foobar, x, y, ...
val INT: TokenType = "INT"   // 1343456

// Operators
val ASSIGN: TokenType = "="
val PLUS: TokenType = "+"
val MINUS: TokenType = "-"
val BANG: TokenType = "!"
val ASTERISK: TokenType = "*"
val SLASH: TokenType = "/"

val LT: TokenType = "<"
val GT: TokenType = ">"

val EQ: TokenType = "=="
val NOT_EQ: TokenType = "!="

// Delimiters
val COMMA: TokenType = ","
val SEMICOLON: TokenType = ";"

val LPAREN: TokenType = "("
val RPAREN: TokenType = ")"
val LBRACE: TokenType = "{"
val RBRACE: TokenType = "}"

// Keywords
val FUNCTION: TokenType = "FUNCTION"
val LET: TokenType = "LET"
val TRUE: TokenType = "TRUE"
val FALSE: TokenType = "FALSE"
val IF: TokenType = "IF"
val ELSE: TokenType = "ELSE"
val RETURN: TokenType = "RETURN"

val STRING: TokenType = "STRING"

data class Token(val type: TokenType, val literal: String)

var keywords = hashMapOf(
    "fn" to FUNCTION,
    "let" to LET,
    "true" to TRUE,
    "false" to FALSE,
    "if" to IF,
    "else" to ELSE,
    "return" to RETURN
)

fun lookupIdent(ident: String): TokenType {
    return keywords.get(ident) ?: IDENT
}
