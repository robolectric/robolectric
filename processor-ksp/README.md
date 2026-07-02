# Robolectric KSP Processor

`processor-ksp` is the [KSP](https://kotlinlang.org/docs/ksp-overview.html) implementation of the
Robolectric Annotation Processor (RAP). It generates the same `Shadows` provider as the javac
[`processor`](../processor/README.md) but runs as a `SymbolProcessor`, which suits shadow packages
written in Kotlin (where `kotlin-kapt` is unavailable under Android Gradle Plugin 9 and built-in
Kotlin).

It is published as a **separate** artifact on purpose: KSP is coupled to a specific Kotlin compiler
version, so isolating it here keeps the javac `processor` artifact — used by Robolectric's own build
and by every javac/`kapt` consumer — free of any Kotlin/KSP version constraint. Only projects that
opt into the KSP path take on that coupling.

## Usage

```kotlin
plugins {
  id("com.android.library")
  id("com.google.devtools.ksp")
}

dependencies { ksp("org.robolectric:processor-ksp:<version>") }

ksp {
  arg(
    "org.robolectric.annotation.processing.shadowPackage",
    "com.example.shadows"
  )
}
```

## Supported processor options

The option keys mirror the javac processor's (the values are part of Robolectric's annotation
processor contract):

* `org.robolectric.annotation.processing.shadowPackage` — Java package for the generated `Shadows`
  class (required).
* `org.robolectric.annotation.processing.shouldInstrumentPackage` — whether the provider advertises
  packages for instrumentation (defaults to `true`).
* `org.robolectric.annotation.processing.priority` — the `@javax.annotation.Priority` value on the
  generated `Shadows` class (defaults to `0`).
