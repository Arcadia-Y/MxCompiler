grammar Mx;		

program
    : (func_decl | class_decl | var_decl)* EOF;

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

Question: '?';
Colon: ':';
Semi: ';';
Comma: ',';

Dot : '.';

IntegerLiteral: Digit+;
fragment EscapeSequence: '\\' [n"\\];
fragment Character: ~["\r\n\\] | EscapeSequence;
StringLiteral: '"' Character*? '"';
