grammar Kong;

@header {
    package antlr;
}

/** The start rule; begin parsing here. */
prog
    : statementList
    ;

statementList
    : ( statement eos? )*
    ;

statement
    : assignmentStatement
    | expressionStatement
    | returnStatement
    | functionDeclStatement
    | ifStatement
    | forStatement
    ;

assignmentStatement
    : 'let' ID indexes? '=' expression
    ;

expressionStatement
    : expression
    ;

returnStatement
    : 'return' expression
    ;

ifStatement
    : ifStat elseIfStat* elseStat?
    ;

ifStat
    : 'if' '(' expression ')' block
    ;

elseIfStat
    : 'else' 'if' '(' expression ')' block
    ;

elseStat
    : 'else' block
    ;

forStatement
    : 'for' '(' ID 'in' start=INT 'to' end=INT ')'  block
    ;

functionDeclStatement
    : 'def' ID '(' idList? ')' '=' expression NEWLINE
    | 'def' ID '(' idList? ')' '=' block
    ;

idList
    : ID ( ',' ID )*
    ;

block
    : '{' statementList '}'
    ;

functionCall
    : ID '(' ( expressionList )? ')'    #identifierFunctionCall
    | 'println' '(' expression? ')'     #printlnFunctionCall
    ;
expressionList
    : expression ( ',' expression )*
    ;

expression
    : '-' expression                #unaryMinusExpression
    | '!' expression                #notExpression
    | expression '^' expression                 #powerExpression
    | expression '*' expression                 #multiplyExpression
    | expression '/' expression                 #divideExpression
    | expression '%' expression                 #modulusExpression
    | expression '+' expression                 #addExpression
    | expression '-' expression                 #subtractExpression
    | expression '>=' expression                #gtEqExpression
    | expression '<=' expression                #ltEqExpression
    | expression '>' expression                 #gtExpression
    | expression '<' expression                 #ltExpression
    | expression '==' expression                #eqExpression
    | expression '!=' expression                #notEqExpression
    | expression '&&' expression                #andExpression
    | expression '||' expression                #orExpression
    | expression '?' expression ':' expression  #ternaryExpression
    | INT                                       #intExpression
    | FLOAT                                     #floatExpression
    | BOOL                                      #boolExpression
    | NULL                                      #nullExpression
    | functionCall                              #functionCallExpression
    | ID indexes?                               #identifierExpression
    | STRING indexes?                           #stringExpression
    | '(' expression ')' indexes?               #expressionExpression
    ;

indexes
    : ('[' expression ']')+
    ;

eos
    : SEMICOLON
    | NEWLINE
    | EOF
    | eos SEMICOLON
    | eos NEWLINE
    ;

COMMA : ',';
SEMICOLON : ';';
NEWLINE : '\r'? '\n';
NULL: 'null' ;

// Identifier
ID  : [a-zA-Z_] [a-zA-Z_0-9]* ;

// String
STRING
    : '"' ( ESC|. )*? '"';

// Comments
SL_COMMENT  : '//' .*? '\r'? '\n' -> skip ; // Match "//" stuff '\n'
ML_COMMENT  : '/*' .*? '*/' -> skip ; // Match "/*" stuff "*/"

// White spaces
WS  : [ \t]+ -> channel(HIDDEN) ; // match 1-or-more whitespace but discard


INT
    : ('0' | '1'..'9' DIGIT*)
    ;

FLOAT
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    ;

// Boolean
BOOL
    : 'true'
    | 'false'
    ;

fragment ESC    : '\\' [btnr"\\] ; // \b, \t, \n etc...

fragment DIGIT  : '0' .. '9' ;

