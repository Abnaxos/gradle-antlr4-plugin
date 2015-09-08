/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Raffael Herzog
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ch.raffael.gradlePlugins.antlr4

import org.gradle.api.GradleException

import java.util.regex.Matcher


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class GrammarFile {

    static Comparator<GrammarFile> COMPILE_ORDER =
            { left, right -> left.type.compileOrder <=> right.type.compileOrder } as Comparator

    File inputFile
    File outputDir

    String packageName

    Type type

    Type readFileType() {
        Matcher matcher = (inputFile.text =~ '(\\s|/\\*(.|[\\n\\r])*\\*/|//.*[\\n\\r])*((?<t>parser|lexer)(\\s|$)+)?grammar[\\s\\n\\r]')
        if (!matcher.find() || matcher.start() != 0) {
            throw new GradleException("Syntax error in ANTLR grammar $file.file; cannot determine grammar type")
        }
        switch ( matcher.group('t') ?: '' ) {
        case '':
            return type = Type.COMBINED
        case 'parser':
            return type = Type.PARSER
        case 'lexer':
            return type = Type.LEXER
        default:
            throw new IllegalStateException("Unreachable code: File $file.file: Unknown grammar type '${matcher.group('t')}'")
        }
    }

    enum Type {
        COMBINED(0), LEXER(-1), PARSER(1);

        /**
         * File ordering: If you're using separate lexer and parser grammars instead of a combined
         * one, lexer grammars need to be compiled first. Lexer grammars produce a file *.tokens
         * which is needed for generating the code for the parser grammar. Combined grammars also
         * produce *.tokens. This gives a quite logical order of compiling *.g4:
         *
         *  1. lexer grammars
         *  2. combined grammars
         *  3. parser grammars
         *
         * Note that this only covers simple cases. It won't help if you're trying to combine
         * grammars in more complex ways.
         */
        final int compileOrder

        private Type(int compileOrder) {
            this.compileOrder = compileOrder
        }
    }

}
