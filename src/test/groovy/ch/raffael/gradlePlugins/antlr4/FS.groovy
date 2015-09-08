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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class FS implements AutoCloseable {

    final Path path
    final File file

    FS(Path source, name = null) {
        name = name ?: source.fileName
        path = Files.createTempDirectory(name.toString() + '.')
        println "Created temp dir: $path"
        file = path.toFile()
        new AntBuilder().copy(todir: path.toString()) {
            fileset(dir: source.toString(), includes: '**')
        }
    }

    FS(File source, name = null) {
        this(source.toPath(), name)
    }

    FS(Object ref, String source, name = null) {
        this(resource(ref, source), name)
    }

    void close() {
        if ( Files.exists(path) ) {
            new AntBuilder().delete(dir: path.toString())
        }
    }

    private static Path resource(Object ref, String resource) {
        Class refClass = ref instanceof Class? ref : ref.getClass()
        URL url = refClass.getResource(resource)
        if ( url == null ) {
            throw new FileNotFoundException("Resource $resource not found relative to class $refClass.name")
        }
        return Paths.get(new URI(url.toString()))
    }

}
