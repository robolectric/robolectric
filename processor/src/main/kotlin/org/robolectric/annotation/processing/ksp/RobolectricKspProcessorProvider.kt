@file:Suppress("SpreadOperator")

package org.robolectric.annotation.processing.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile

/**
 * KSP processor option specifying the Java package for the generated `Shadows` class. Required.
 *
 * Example: `ksp { arg("org.robolectric.annotation.processing.shadowPackage", "com.example") }`
 */
// Duplicated in RobolectricProcessor.java (KAPT/javac implementation)
private const val PACKAGE_OPT = "org.robolectric.annotation.processing.shadowPackage"

/**
 * KSP processor option controlling whether the generated provider advertises packages for
 * instrumentation via [org.robolectric.internal.ShadowProvider.getProvidedPackageNames]. Defaults
 * to `true`. Set to `"false"` to disable.
 */
// Duplicated in RobolectricProcessor.java (KAPT/javac implementation)
private const val SHOULD_INSTRUMENT_PKG_OPT =
  "org.robolectric.annotation.processing.shouldInstrumentPackage"

/**
 * KSP processor option setting the `@javax.annotation.Priority` value on the generated `Shadows`
 * class. Higher-priority providers override lower-priority ones for the same shadow target.
 * Defaults to `0` (no `@Priority` annotation emitted).
 */
// Duplicated in RobolectricProcessor.java (KAPT/javac implementation)
private const val PRIORITY_OPT = "org.robolectric.annotation.processing.priority"

class RobolectricKspProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    RobolectricKspProcessor(environment)
}

private class RobolectricKspProcessor(private val environment: SymbolProcessorEnvironment) :
  SymbolProcessor {
  private var generated = false

  @Suppress("ReturnCount")
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val shadowPackage = environment.options[PACKAGE_OPT]
    if (shadowPackage.isNullOrBlank()) {
      if (!generated) {
        environment.logger.error("no package specified with $PACKAGE_OPT")
      }
      return emptyList()
    }

    val shouldInstrumentPackages =
      !"false".equals(environment.options[SHOULD_INSTRUMENT_PKG_OPT], ignoreCase = true)
    val priority = environment.options[PRIORITY_OPT]?.toIntOrNull() ?: 0

    val sourceFiles = linkedSetOf<KSFile>()
    val shadowInfos = mutableListOf<ShadowInfo>()
    val resetterInfos = mutableListOf<ResetterInfo>()

    resolver.getSymbolsWithAnnotation(IMPLEMENTS_ANNOTATION).forEach { symbol ->
      val declaration = symbol as? KSClassDeclaration ?: return@forEach
      if (declaration.classKind != ClassKind.CLASS) {
        return@forEach
      }

      declaration.containingFile?.let(sourceFiles::add)
      val shadowInfo = declaration.toShadowInfo()
      if (shadowInfo == null) {
        environment.logger.error(
          "Unable to resolve @Implements target for '${declaration.qualifiedName?.asString()}'"
        )
        return@forEach
      }
      shadowInfos += shadowInfo
      resetterInfos += declaration.directStaticResetters(shadowInfo, environment.logger)
      resetterInfos += declaration.companionObjectResetters(shadowInfo)
    }

    if (shadowInfos.isNotEmpty() && !generated) {
      val dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray())
      writeShadowProvider(
        codeGenerator = environment.codeGenerator,
        dependencies = dependencies,
        shadowPackage = shadowPackage,
        shouldInstrumentPackages = shouldInstrumentPackages,
        priority = priority,
        shadowInfos = shadowInfos,
        resetterInfos = resetterInfos,
      )
      writeServiceFile(
        codeGenerator = environment.codeGenerator,
        dependencies = dependencies,
        shadowPackage = shadowPackage,
      )
      generated = true
    }

    return emptyList()
  }
}
