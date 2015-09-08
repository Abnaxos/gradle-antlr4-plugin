The ANTLR4 Gradle Plugin
========================

This is a Gradle plugin that adds support for ANTLR4 to Gradle. I'm aware that there are already plugins that claim to do that, however, not to my satisfaction. There are some features that I miss from the existing plugins:

 *  Correct placement of generated source files. The generated sources of `src/main/antlr4/my/package/MyGrammar.g4` needs to be placed into `$outputDir/my/package/MyGrammarLexer.java` (or Parser).
 
 *  Ability to change the package where to find the runtime classes. As the version of the runtime needs to match the version of the generator, shadowing (AKA shading) the runtime is a *must*. I myself perfer pre-shadowing (see the [Preshadow Gradle Plugin](https://github.com/Abnaxos/gradle-preshadow-plugin)), so the generated classes must be changed to use the new package of the runtime classes. So I added this as a built-in feature.
 
 
Usage
-----

```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath group:'ch.raffael.gradlePlugins.antlr4', name:'gradle-antlr4-plugin', version:'1.0'
  }
}

apply plugin:'ch.raffael.antlr4'
```

This will automatically compile the grammars under `src/main/antlr4` to `$buildDir/antlr4/src`,
add it to the sources for `compileJava` and set the dependencies correctly.

Configuration options and their defaults:

```gradle
antlr4 {
  // The version of the ANTLR tool to use; you can mix these using the runtimePackage argument
  version = '4.5'
  
  // what to generate
  listener = true
  visitor = true
  
  // what package to find the runtime classes in; best used with preshadow
  runtimePackage = 'org.antlr.v4.runtime'
  
  // where to put the generated classes
  destination = "$buildDir/antlr4/src"
  
  // where to put intermediate files
  generateDir = "$buildDir/antlr4/gen"
}
```


Compile Order
-------------

The plugin orders the grammar files as follows:

 1. lexer grammars
 2. combined grammars
 3. parser grammars

This is a very simple mechanism that makes sure that split grammars will compile correctly. For more complex cases, however, this may not be enough, as the plugin currently doesn't build a dependency graph based on the `import` statements. This may be added to the plugin at a later point.


Preshadow Example
-----------------

```gradle
buildscript {
  repositories {
    jcenter()
  }
  dependencies {
    classpath group:'ch.raffael.gradlePlugins.antlr4', name:'gradle-antlr4-plugin', version:'1.0'
    classpath group:'ch.raffael.gradlePlugins.preshadow', name:'gradle-preshadow-plugin', version:'1.0'
  }
}

apply plugin:'java'
apply plugin:'ch.raffael.antlr4'
apply plugin:'ch.raffael.preshadow'

def antlrVersion = '4.5'

repositories {
  jcenter()
}

dependencies {
  preshadow group:'org.antlr', name:'antlr4-runtime', version:antlrVersion
}

preshadowJar {
  relocate 'org.antlr.v4.runtime', 'my.dsl.antlr4rt'
  exclude 'org/abego/**'
}

antlr4 {
  version = antlrVersion
  visitor = false
  runtimePackage = 'my.dsl.antlr4rt'
}

```
