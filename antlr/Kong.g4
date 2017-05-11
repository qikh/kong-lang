grammar Kong;

/** The start rule; begin parsing here. */
prog:   statement_list ;

stmt: simple_stmt;

simple_stmt: small_stmt;

small_stmt: (let_stmt | expr_stmt | return_stmt);

let_stmt:  'let' ID '=' expression;

expr_stmt: expression;

return_stmt:    'return' expression;

operand
    : literal
    | operandName
    | '(' expression ')'
    ;

literal
    : basic_lit
    | function_lit
    | function_call_lit
    ;

basic_lit
    : INT
    | FLOAT
    | STRING
    ;

function_lit
    :   ID parameters block
    ;

parameters
    : '(' ( parameterList )? ')'
    ;

parameterList
    : parameterDecl ( ',' parameterDecl )*
    ;

parameterDecl
    : ID
    ;

function_call_lit
    :   ID arguments
    ;

arguments
    : '(' ( argumentList )? ')'
    ;

argumentList
    : argumentDecl ( ',' argumentDecl )*
    ;

argumentDecl
    : ID
    | basic_lit
    ;

operandName
    : ID
    | qualifiedIdent
    ;

primaryExpr
    : operand
    ;


block
    : '{' statement_list '}'
    ;

statement_list
    : ( stmt eos)*
    ;

//QualifiedIdent = PackageName "." identifier .
qualifiedIdent
    : ID '.' ID
    ;

expression:   unaryExpr
    |   expression ('||' | '&&' | '==' | '!=' | '<' | '<=' | '>' | '>=' | '+' | '-' | '|' | '^' | '*' | '/' | '%' | '<<' | '>>' | '&' | '&^') expression
    ;

unaryExpr:  primaryExpr
    |   ('+'|'-'|'!'|'^'|'*'|'&'|'<-') unaryExpr;

eos
    : eos SEMICOLON
    | eos NEWLINE
    | SEMICOLON
    | NEWLINE
    | EOF
    ;

COMMA : ',';
SEMICOLON : ';';
NEWLINE : '\r'? '\n';

// Identifier
ID : ID_LETTER (ID_LETTER | DIGIT)* ; // From C language
fragment ID_LETTER : 'a'..'z'|'A'..'Z'|'_' ;
fragment DIGIT : '0'..'9' ;

// Numbers
INT : DIGIT+ ;
FLOAT
    : DIGIT+ '.' DIGIT* | '.' DIGIT+
    ;

// String
STRING :    '"' ( ESC|. )*? '"';
fragment ESC : '\\' [btnr"\\] ; // \b, \t, \n etc...

// White spaces
WS : [ \t\n\r]+ -> skip ; // match 1-or-more whitespace but discard

// Comments
LINE_COMMENT : '//' .*? '\r'? '\n' -> skip ; // Match "//" stuff '\n'
COMMENT : '/*' .*? '*/' -> skip ; // Match "/*" stuff "*/"
