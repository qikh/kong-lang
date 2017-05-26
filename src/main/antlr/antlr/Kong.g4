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
    | whileStatement
    | eos
    ;

assignmentStatement
    : 'let'? Identifier indexes? '=' expression
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
    : 'for' '(' Identifier 'in' expression 'to' expression ')'  block
    ;

whileStatement
    : 'while' '(' expression ')' block
    ;

functionDeclStatement
    : 'def' Identifier '(' idList? ')' '=' expression Newline
    | 'def' Identifier '(' idList? ')' '=' block
    ;

idList
    : Identifier ( ',' Identifier )*
    ;

block
    : '{' statementList '}'
    ;

functionCall
    : Identifier '(' ( expressionList )? ')'    #identifierFunctionCall
    | Println '(' expression? ')'  #printlnFunctionCall
    | Print '(' expression ')'     #printFunctionCall
    | Assert '(' expression ')'    #assertFunctionCall
    | Size '(' expression ')'      #sizeFunctionCall
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
    | expression 'in' expression                #inExpression
    | Int                                       #intExpression
    | Float                                     #floatExpression
    | Bool                                      #boolExpression
    | Null                                      #nullExpression
    | functionCall                              #functionCallExpression
    | list indexes?                             #listExpression
    | Identifier indexes?                               #identifierExpression
    | String indexes?                           #stringExpression
    | '(' expression ')' indexes?               #expressionExpression
    ;

list
    : '[' expressionList? ']'
    ;

indexes
    : ('[' expression ']')+
    ;

eos
    : eos Semicolon
    | eos Newline
    | Semicolon
    | Newline
    ;

Println  : 'println';
Print    : 'print';
Input    : 'input';
Assert   : 'assert';
Size     : 'size';

Comma : ',';
Semicolon : ';';
Newline : '\r'? '\n';
Null: 'null' ;


Int
    : ('0' | [1-9] Digit*)
    ;

Float
    : Int ('.' Digit*)?
    ;

Bool
    : 'true'
    | 'false'
    ;

Identifier  : [a-zA-Z_] [a-zA-Z_0-9]* ;

String
    : ["] (~["\r\n] | '\\\\' | '\\"')* ["]
    | ['] (~['\r\n] | '\\\\' | '\\\'')* [']
    ;

Comment
    : ('//' ~[\r\n]* | '/*' .*? '*/') -> skip
    ;

Space
    : [ \t] -> skip
    ;

fragment Digit  : '0' .. '9' ;

