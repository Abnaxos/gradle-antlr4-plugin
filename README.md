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


Future
------

Also, I'll add a feature to compile lexer grammars before parser grammars. A parser grammar won't compile if its lexer grammar hasn't been compiled yet. A future version of the plugin will pre-scan the source files and ensure the following order in compilation:

 *  `lexer grammar X;`
 *  `grammar X;` (combined grammar)
 *  `parser grammar X;`

I'm not sure about how to handle grammer imports yet. But there are definitely plans to handle these ANTLR features correctly.
