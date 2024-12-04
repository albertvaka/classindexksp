# ClassIndex KSP

<img src="https://img.shields.io/static/v1.svg?label=no%20tests&message=always%20green&color=green" />

## About

ClassIndex KSP lets you index your classes at compile time, so you can find them at runtime without having to scan the classpath or use reflection.

It is a fast, modern, Kotlin-based, and KSP-based alternative to [atteo/classindex](https://github.com/atteo/classindex) or [matfax/klassindex](https://github.com/matfax/klassindex). Here's a comparison of the three:

| Aspects                     | ClassIndex                        | KlassIndex                             | ClassIndex KSP                                 |
|-----------------------------|-----------------------------------|----------------------------------------|------------------------------------------------|
| Language                    | Java                              | Kotlin                                 | Kotlin                                         |
| Annotation processor        | KAPT                              | KAPT                                   | KSP                                            |
| Supported Build Tools       | Maven and Gradle                  | Gradle                                 | Gradle                                         |
| Supported Scopes            | Annotations, Subclasses, Packages | Annotations, Subclasses                | Annotations only, Subclass support to be added |
| Requires runtime dependency | Yes                               | Yes                                    | No                                             |
| Service Loader Support      | Yes                               | No                                     | No                                             |
| JaxB Index                  | Yes                               | No                                     | No                                             |
| Stores Documentation        | Yes                               | No                                     | No                                             |
| Android Support             | Yes                               | Claimed but doesn't work with R8       | Yes                                            |
| Runtime Performance         | Fair                              | Good                                   | The greatest ever                              |
| Compile-time Performance    | Bad                               | Bad                                    | The best under the Sun                         |
| Filtering Support           | Limited                           | Yes, using Kotlin's `Iterable` filters | Yes, using Kotlin's `Iterable` filters         |
| Index 3rd party classes     | Yes, by extending the processor   | Yes, using kapt arguments              | No                                             |
| Compile Time Safety         | Limited                           | Complete                               | Complete                                       |
| License                     | Apache 2.0                        | Apache 2.0                             | Apache 2.0                                     |

## How to use it?

### Add the dependency in your gradle build files

Add Jitpack as a plugin repository in `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        ...
        maven { setUrl("https://jitpack.io") }
        ...
    }
}
```

In `build.gradle.kts`, add the KSP plugin if you don't have it already. Check the [official docs](https://kotlinlang.org/docs/ksp-quickstart.html) for the latest version.

```kotlin
plugins {
    ...
    id("com.google.devtools.ksp") version "2.+"
    ...
}
```

Also in `build.gradle.kts`, and a KSP dependency on `com.github.albertvaka:classindexksp`

```kotlin
dependencies {
    ...
    ksp("com.github.albertvaka:classindexksp:1.+")
    ...
}
```

### Specify the Annotations to index

Define one or more annotations in your code and use them to annotate the classes that you want to index

```kotlin
package com.example

annotation class MyAnnotation

@MyAnnotation
class MyClass {
    ...
}

@MyAnnotation
class MyOtherClass {
    ...
}
```

Pass the fully-qualified name of the annotations to index as a comma-separated string in the `com.albertvaka.classindexksp.annotations` KSP argument in your `build.gradle.kts`

```kotlin
ksp {
    arg("com.albertvaka.classindexksp.annotations", "com.example.MyAnnotation")
}
```

### Access the generated indices

KSP will generate code in the package `com.albertvaka.classindexksp` with a set of your annotated classes for each of your annotations. For example, for the `MyAnnotation` example above you can access the index as `com.albertvaka.classindexksp.MyAnnotation`. The generated code will look like this:

```kotlin
package com.albertvaka.classindexksp

val MyAnnotation = setOf(
    com.example.MyClass::class,
    com.example.MyOtherClass::class,
)
```

## Why ClassIndex KSP

### Runtime speed

Traditional classpath scanning is a [very](https://www.leveluplunch.com/blog/2015/08/11/reducing-startup-times-spring-applications-context/)
[slow](https://wiki.apache.org/tomcat/HowTo/FasterStartUp) process.
Replacing it with compile-time indexing speeds Java applications bootstrap considerably.

Here are the results of the [benchmark](https://github.com/atteo/classindex-benchmark) comparing ClassIndex with various scanning solutions.

| Library                                               | Application startup time |
|-------------------------------------------------------|-------------------------:|
| None - hardcoded list                                 |                  0:00.18 |
| [Scannotation](http://scannotation.sourceforge.net/)  |                  0:05.11 |
| [Reflections](https://github.com/ronmamo/reflections) |                  0:05.37 |
| Reflections Maven plugin                              |                  0:00.52 |
| ClassIndex                                            |                  0:00.18 |

Notes: benchmark was performed on Intel i5-2520M CPU @ 2.50GHz, classpath size was set to 121MB.

### Compile-time speed

By using the Kotlin Symbol Processing (KSP) instead of the deprecated Kotlin Annotation Processing Tool (KAPT), compile times are faster with ClassIndex KSP compared to its alternatives.
