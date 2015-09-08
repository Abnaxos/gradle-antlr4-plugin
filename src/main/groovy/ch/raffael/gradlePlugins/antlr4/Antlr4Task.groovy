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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.tasks.*

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class Antlr4Task extends DefaultTask {

    public static final String STANDARD_RUNTIME_PACKAGE = 'org.antlr.v4.runtime'

    private static final Map<String,Integer> GRAMMAR_TYPE_ORDERING = [
            parser: 1,
            lexer: -1,
    ]

    @Input
    @Optional
    String version = '4.5'

    @InputDirectory
    @Optional
    File source = project.file('src/main/antlr4')

    @Optional
    File generateDir = project.file("$project.buildDir/antlr4/gen")

    @OutputDirectory
    @Optional
    File destination = project.file("$project.buildDir/antlr4/src")

    @Input
    @Optional
    boolean listener = true
    @Input
    @Optional
    boolean visitor = true

    @Input
    @Optional
    String runtimePackage = STANDARD_RUNTIME_PACKAGE

    private final static Map<Set<File>, ToolRunner> RUNNER_CACHE = [:]

    @TaskAction
    void generateAndTransformAntlr4() {
        project.delete(generateDir)
        project.mkdir(generateDir)
        generateAntlr4()
        project.copy {
            into { destination }
            from { generateDir }
            include '**/*.java'

            if ( runtimePackage != STANDARD_RUNTIME_PACKAGE ) {
                filter { line ->
                    line.replaceAll(
                            "([^\\p{javaJavaIdentifierStart}]|^)${Pattern.quote(STANDARD_RUNTIME_PACKAGE)}([^\\p{javaJavaIdentifierPart}]|\$)",
                            "\$1${Matcher.quoteReplacement(runtimePackage)}\$2")
                }
            }
        }
    }

    private void generateAntlr4() {
        Set<File> antlrClasspath = project.with {
            configurations.detachedConfiguration(
                    dependencies.create(group: 'org.antlr', name: 'antlr4', version: this.version)
            )
        }.files
        ToolRunner runner
        synchronized ( RUNNER_CACHE ) {
            runner = RUNNER_CACHE[antlrClasspath]
            if ( runner == null ) {
                logger.debug("Creating new ANTLR4 ToolRunner for classpath {}", antlrClasspath)
                runner = new ToolRunner(antlrClasspath)
                RUNNER_CACHE[Collections.unmodifiableSet(new HashSet<File>(antlrClasspath))] = runner
            }
        }
        boolean hadError = false
        List<GrammarFile> grammarFiles = []
        project.fileTree(dir:source, include:'**/*.g4').visit { FileVisitDetails file ->
            if (file.isDirectory()) {
                return
            }
            def grammarFile = new GrammarFile(
                    inputFile: file.file,
                    outputDir: project.file("$generateDir/$file.relativePath.parent"),
                    packageName: file.relativePath.parent.segments.join('.'))
            grammarFile.readFileType()
            grammarFiles.add grammarFile
        }
        grammarFiles.sort(true, GrammarFile.COMPILE_ORDER).each { file ->
            project.mkdir(file.outputDir)
            def antlrArgs = []
            antlrArgs << '-o' << file.outputDir
            antlrArgs << '-package' << file.packageName
            antlrArgs << (listener ? '-listener' : '-no-listener')
            antlrArgs << (visitor ? '-visitor' : '-no-visitor')
            antlrArgs << file.inputFile
            logger.debug('Calling ANTLR with arguments {}', antlrArgs)
            if ( !runner.run(logger, antlrArgs as String[]) ) {
                hadError = true
            }
        }
        if ( hadError ) {
            throw new GradleException("Some grammars contained ANTLR errors")
        }
    }

}
