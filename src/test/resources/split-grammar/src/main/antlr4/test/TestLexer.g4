lexer grammar TestLexer;

ID: [a-zA-Z] [a-zA-Z0-0]*;
INT: [0-9]+;
WS: [ \t\r\n]+ -> skip;
