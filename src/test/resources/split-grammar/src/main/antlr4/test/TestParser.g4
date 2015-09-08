parser grammar TestParser;

options {
    tokenVocab = TestLexer;
}

root: (ID|INT)+;
