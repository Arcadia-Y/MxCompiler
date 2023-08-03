grammar Mx;		

@header { 
package Parser;
}

program
    :   declaration* EOF
    ;

declaration
    :   varDecl
    |   funcDecl
    |   classDecl
    ;

statement
    :   blockStmt  
    |   varDeclStmt     
    |   ifStmt      
    |   loopStmt    
    |   jumpStmt    
    |   exprStmt
    ;

blockStmt
    :   '{' statement* '}'
    ;

exprStmt
    :   expr? ';'
    ;

ifStmt
    :   'if' '(' cond=expr ')' trueStmt=statement ('else' falseStmt=statement)?
    ;

loopStmt
    :   'while' '(' expr ')' statement # While
    |   'for' '(' init = exprStmt
                  cond = expr? ';'
                  next = expr? ')'
                statement              # For
    |   'for' '(' declInit = varDecl
                  cond = expr? ';'
                  next = expr? ')'
                statement              # For
    ;

jumpStmt
    :   'continue' ';'     # Continue
    |   'break' ';'        # Break
    |   'return' expr? ';' # Return
    ;

varDeclStmt
    : varDecl
    ;

varDecl
    :   type varInitDecl (',' varInitDecl)* ';'
    ;

varInitDecl
    :   Identifier ('=' expr)?
    ;

nonArray
    :   BultinType
    |   Identifier
    ;

type
    :   nonArray ('[' ']')* 
    ;

funcDecl
    :   type Identifier '(' (parameterDecl (',' parameterDecl)*)? ')' blockStmt
    ;

parameterDecl
    :   type Identifier
    ;

classDecl
    :   'class' Identifier '{' memberDecl* '}' ';'
    ;
 
memberDecl
    :   varDecl
    |   funcDecl
    |   constructDecl
    ;

constructDecl
    :   Identifier '(' ')' blockStmt
    ;

// reference: https://en.cppreference.com/w/cpp/language/operator_precedence
expr
    :   '(' expr ')'                                    # subExpr

    |   expr op=('++'|'--')                             # postfix
    |   expr '(' (expr (',' expr)*)? ')'                # functionCall
    |   expr '[' index=expr ']'                         # subscript
    |   expr  '.' Identifier                            # memberAccess

    |   <assoc=right> op=('++' | '--') expr             # unaryExpr
    |   <assoc=right> op=('+' | '-') expr               # unaryExpr
    |   <assoc=right> op=('!' | '~') expr               # unaryExpr
    |   <assoc=right> 'new' newItem                     # newExpr

    |   l=expr op=('*' | '/' | '%') r=expr              # binaryExpr
    |   l=expr op=('+' | '-') r=expr                    # binaryExpr
    |   l=expr op=('<<' | '>>') r=expr                  # binaryExpr
    |   l=expr op=('<=' | '>=' | '<' | '>') r=expr      # binaryExpr
    |   l=expr op=('<' | '>') r=expr                    # binaryExpr
    |   l=expr op=('==' | '!=') r=expr                  # binaryExpr
    |   l=expr op='&' r=expr                            # binaryExpr
    |   l=expr op='^' r=expr                            # binaryExpr
    |   l=expr op='|' r=expr                            # binaryExpr
    |   l=expr op='&&' r=expr                           # binaryExpr
    |   l=expr op='||' r=expr                           # binaryExpr

    |   <assoc=right> expr '?' expr ':' expr            # ternary
    |   <assoc=right> expr '=' expr                     # assignment

    |   This                                            # variable
    |   Identifier                                      # variable
    |   literal                                         # constant
    ;

newItem
    :   nonArray ('[' expr ']')+ ('['']')*
    |   Identifier ('('')')?                            
    ;

literal
    :   'true'
    |   'false'
    |   'null'
    |   StringLiteral
    |   IntegerLiteral
    ;


// keywords
BultinType: 'void' | 'bool' | 'int' | 'string';
New: 'new';
Class: 'class';
Null: 'null';
True: 'true';
False: 'false';
This: 'this';
If: 'if';
Else: 'else';
For: 'for';
While: 'while';
Break: 'break';
Continue: 'continue';
Return: 'return';

Whitespace: [ \t]+ -> skip;
Newline:('\r' '\n'? | '\n') -> skip;
LineComment: '//' ~[\r\n]* -> skip;
BlockComment: '/*' .*? '*/' -> skip;

fragment Letter: [a-zA-Z];
fragment Digit: [0-9];
Identifier: Letter (Letter | Digit | '_')*;

LeftParen: '(';
RightParen: ')';
LeftBracket: '[';
RightBracket: ']';
LeftBrace: '{';
RightBrace: '}';

Less: '<';
LessEqual: '<=';
Greater: '>';
GreaterEqual: '>=';
LeftShift: '<<';
RightShift: '>>';

Plus: '+';
PlusPlus: '++';
Minus: '-';
MinusMinus: '--';
Star: '*';
Div: '/';
Mod: '%';

And: '&';
Or: '|';
AndAnd: '&&';
OrOr: '||';
Caret: '^';
Not: '!';
Tilde: '~';

Equal: '==';
NotEqual: '!=';
Assign: '='; 

Question: '?';
Colon: ':';
Semi: ';';
Comma: ',';

Dot : '.';

IntegerLiteral: Digit+;
fragment EscapeSequence: '\\' [n"\\];
fragment Character: ~["\r\n\\] | EscapeSequence;
StringLiteral: '"' Character*? '"';
