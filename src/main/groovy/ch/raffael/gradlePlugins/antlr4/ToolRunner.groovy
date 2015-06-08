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

import org.gradle.api.Task
import org.slf4j.Logger


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class ToolRunner {

    final ClassLoader classLoader
    final Class runnerClass

    ToolRunner(Set<File> classpath) {
        classLoader = new URLClassLoader(classpath.collect({file -> file.toURI().toURL()}) as URL[], Task.class.getClassLoader())
        runnerClass = new GroovyClassLoader(classLoader).parseClass(RUNNER_SOURCE)
    }

    public boolean run(Logger logger, String[] args) {
        runnerClass.run(logger, args)
    }

    private static final String RUNNER_SOURCE = """
        package ch.raffael.gradlePlugins.antlr4.___runner___
        import org.antlr.v4.Tool
        import org.antlr.v4.tool.ANTLRMessage
        import org.antlr.v4.tool.ANTLRToolListener
        class ToolRunner {
            static boolean run(logger, String[] args) {
                Tool tool = new Tool(args)
                tool.addListener(new ANTLRToolListener() {
                    @Override
                    void info(String msg) {
                        logger.info(msg)
                    }
                    @Override
                    void error(ANTLRMessage msg) {
                        logger.error(tool.errMgr.getMessageTemplate(msg).render())
                    }
                    @Override
                    void warning(ANTLRMessage msg) {
                        logger.warn(tool.errMgr.getMessageTemplate(msg).render())
                    }
                })
                tool.processGrammarsOnCommandLine()
                return tool.errMgr.numErrors == 0
            }
        }
        """
}
