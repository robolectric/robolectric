package org.robolectric.annotation.processing.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.util.Locale

internal const val IMPLEMENTS_ANNOTATION = "org.robolectric.annotation.Implements"
private const val RESETTER_ANNOTATION = "org.robolectric.annotation.Resetter"
private const val JVM_STATIC_ANNOTATION = "kotlin.jvm.JvmStatic"
private const val DEFAULT_SHADOW_PICKER =
  "org.robolectric.annotation.Implements.DefaultShadowPicker"
private const val DEFAULT_SHADOW_PICKER_BINARY =
  "org.robolectric.annotation.Implements\$DefaultShadowPicker"

/** Collects direct (top-level `@JvmStatic`) `@Resetter` methods from [this] declaration. */
internal fun KSClassDeclaration.directStaticResetters(
  shadowInfo: ShadowInfo,
  logger: KSPLogger,
): List<ResetterInfo> =
  getDeclaredFunctions()
    .filter { it.hasAnnotation(RESETTER_ANNOTATION) }
    .onEach { function ->
      if (!function.modifiers.contains(Modifier.JAVA_STATIC)) {
        logger.error("Resetter should be static.")
      }
    }
    .filter { it.modifiers.contains(Modifier.JAVA_STATIC) }
    .map { function ->
      ResetterInfo(
        methodCall = "${shadowInfo.shadowBinaryName}.${function.simpleName.asString()}();",
        minSdk = shadowInfo.minSdk,
        maxSdk = shadowInfo.maxSdk,
      )
    }
    .toList()

/** Collects `@JvmStatic @Resetter` methods from the companion object of [this] declaration. */
internal fun KSClassDeclaration.companionObjectResetters(
  shadowInfo: ShadowInfo
): List<ResetterInfo> =
  declarations
    .filterIsInstance<KSClassDeclaration>()
    .filter { it.isCompanionObject }
    .flatMap { companion ->
      companion
        .getDeclaredFunctions()
        .filter { it.hasAnnotation(RESETTER_ANNOTATION) && it.hasAnnotation(JVM_STATIC_ANNOTATION) }
        .map { function ->
          ResetterInfo(
            methodCall = "${shadowInfo.shadowBinaryName}.${function.simpleName.asString()}();",
            minSdk = shadowInfo.minSdk,
            maxSdk = shadowInfo.maxSdk,
          )
        }
    }
    .toList()

/**
 * Returns a [ShadowInfo] for this `@Implements`-annotated class, or `null` if the target cannot be
 * resolved.
 *
 * **Note on `isInAndroidSdk`**: The KSP processor reads the `isInAndroidSdk` attribute and stores
 * it in [ShadowInfo.isInAndroidSdk] for model parity with the Java annotation processor. However,
 * this value currently does not affect the generated output because the KSP processor never
 * generates `shadowOf()` helper methods (unlike the Java AP which skips them when `isInAndroidSdk =
 * false`). The shadow is always registered in the `SHADOWS` list and its package is always included
 * in `getProvidedPackageNames()`.
 */
@Suppress("ReturnCount")
internal fun KSClassDeclaration.toShadowInfo(): ShadowInfo? {
  val implementsAnnotation = findAnnotation(IMPLEMENTS_ANNOTATION) ?: return null
  val className = implementsAnnotation.stringArgument("className")
  val valueClassName = implementsAnnotation.typeArgument("value")
  val actualName = className.ifBlank { valueClassName }
  if (actualName.isNullOrBlank()) {
    return null
  }

  val shadowPickerBinaryName = implementsAnnotation.typeArgument("shadowPicker")
  return ShadowInfo(
    actualName = actualName,
    shadowBinaryName = binaryName(),
    shadowPickerBinaryName =
      shadowPickerBinaryName.takeUnless {
        it.isNullOrBlank() ||
          it == DEFAULT_SHADOW_PICKER ||
          it == DEFAULT_SHADOW_PICKER_BINARY ||
          it == "kotlin.Unit"
      },
    isInAndroidSdk = implementsAnnotation.booleanArgument("isInAndroidSdk", defaultValue = true),
    minSdk = implementsAnnotation.intArgument("minSdk", -1),
    maxSdk = implementsAnnotation.intArgument("maxSdk", -1),
  )
}

private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean =
  findAnnotation(annotationName) != null

private fun KSAnnotated.findAnnotation(annotationName: String): KSAnnotation? =
  annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
  }

private fun KSAnnotation.stringArgument(name: String): String =
  arguments.firstOrNull { it.name?.asString() == name }?.value as? String ?: ""

private fun KSAnnotation.intArgument(name: String, defaultValue: Int): Int =
  (arguments.firstOrNull { it.name?.asString() == name }?.value as? Int) ?: defaultValue

private fun KSAnnotation.booleanArgument(name: String, defaultValue: Boolean): Boolean =
  (arguments.firstOrNull { it.name?.asString() == name }?.value as? Boolean) ?: defaultValue

@Suppress("ReturnCount")
private fun KSAnnotation.typeArgument(name: String): String? {
  val value = arguments.firstOrNull { it.name?.asString() == name }?.value as? KSType ?: return null
  val declaration = value.declaration as? KSClassDeclaration ?: return null
  val qualifiedName =
    declaration.qualifiedName?.asString()?.ifBlank { null } ?: declaration.binaryName()
  return when (qualifiedName.lowercase(Locale.ROOT)) {
    "kotlin.unit",
    "java.lang.void",
    "void" -> null
    else -> declaration.binaryName()
  }
}

private fun KSClassDeclaration.binaryName(): String {
  val nestedNames = mutableListOf<String>()
  var declaration: KSDeclaration? = this
  while (declaration is KSClassDeclaration) {
    nestedNames += declaration.simpleName.asString()
    declaration = declaration.parentDeclaration
  }
  val packageName = packageName.asString()
  val classPart = nestedNames.asReversed().joinToString("$")
  return if (packageName.isBlank()) classPart else "$packageName.$classPart"
}
