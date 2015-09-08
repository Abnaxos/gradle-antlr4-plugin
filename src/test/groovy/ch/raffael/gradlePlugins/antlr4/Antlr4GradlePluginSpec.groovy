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

import org.gradle.tooling.GradleConnector
import spock.lang.Specification


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Antlr4GradlePluginSpec extends Specification {

    static final String PLUGIN_JAR = 'pluginJar'

    def setup() {
        if ( !System.getProperty(PLUGIN_JAR) ) {
            throw new Exception("System property 'pluginJar' not set")
        }
        println "Plugin JAR (Spock): ${System.getProperty(PLUGIN_JAR)}"
    }

    def "Split grammars will be processed in the correct order (Lexers first)"() {
      given:
        def fs = new FS(this, '/split-grammar')
      when:
        GradleConnector.newConnector().
                forProjectDirectory(fs.file).
                connect().
                newBuild().
                setStandardOutput(System.out).
                setStandardError(System.err).
                withArguments("-DpluginJar=${System.getProperty(PLUGIN_JAR)}").
                forTasks('antlr4').
                run()
      then:
        def files = [ 'TestLexer.java',
                      'TestParserBaseListener.java',
                      'TestParserBaseVisitor.java',
                      'TestParser.java',
                      'TestParserListener.java',
                      'TestParserVisitor.java'] as Set
        new File(fs.file, 'build/antlr4/src/test').listFiles().collect({f -> f.name}) as Set == files

      cleanup:
        fs?.close()
    }

}
