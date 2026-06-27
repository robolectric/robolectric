package org.robolectric.annotation.processing.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
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
    .filter { function ->
      // Error rather than throw, matching the javac ResetterValidator.
      val isStatic = function.modifiers.contains(Modifier.JAVA_STATIC)
      if (!isStatic) {
        logger.error("@Resetter methods must be static", function)
      }
      isStatic
    }
    .map { function ->
      ResetterInfo(
        methodCall = "${shadowInfo.shadowBinaryName}.${function.simpleName.asString()}();",
        minSdk = shadowInfo.minSdk,
        maxSdk = shadowInfo.maxSdk,
        shadowClassName = shadowInfo.shadowBinaryName,
        actualBinaryName = shadowInfo.actualName,
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
            shadowClassName = shadowInfo.shadowBinaryName,
            actualBinaryName = shadowInfo.actualName,
          )
        }
    }
    .toList()

/**
 * Returns a [ShadowInfo] for this `@Implements`-annotated class, or `null` if no shadowed type is
 * specified.
 *
 * The shadowed class may be given as a `Class` literal (`value`) or, for hidden classes, as a
 * binary-name string (`className`). [ShadowInfo.actualName] is normalized to the binary name
 * (`$`-separated for nested classes); a `className` that does not resolve on the processor
 * classpath falls back to the raw string, as in the javac processor, so shadows of classes absent
 * from the compile-time SDK still register.
 */
@Suppress("ReturnCount")
internal fun KSClassDeclaration.toShadowInfo(resolver: Resolver): ShadowInfo? {
  val implementsAnnotation = findAnnotation(IMPLEMENTS_ANNOTATION) ?: return null
  val className = implementsAnnotation.typedArgument("className", "")
  val actualDeclaration =
    if (className.isNotBlank()) {
      resolver.getClassDeclarationByName(resolver.getKSNameFromString(className.replace('$', '.')))
    } else {
      implementsAnnotation.typeDeclaration("value")
    }
  val actualName = actualDeclaration?.binaryName() ?: className.ifBlank { null } ?: return null

  val shadowPickerBinaryName = implementsAnnotation.typeDeclaration("shadowPicker")?.binaryName()
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
    isInAndroidSdk = implementsAnnotation.typedArgument("isInAndroidSdk", defaultValue = true),
    minSdk = implementsAnnotation.typedArgument("minSdk", -1),
    maxSdk = implementsAnnotation.typedArgument("maxSdk", -1),
    actualIsPublic = actualDeclaration?.isPublic() == true,
  )
}

private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean =
  findAnnotation(annotationName) != null

private fun KSAnnotated.findAnnotation(annotationName: String): KSAnnotation? =
  annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
  }

private inline fun <reified T> KSAnnotation.typedArgument(name: String): T? =
  arguments.firstOrNull { it.name?.asString() == name }?.value as? T

private inline fun <reified T> KSAnnotation.typedArgument(name: String, defaultValue: T): T =
  typedArgument<T>(name) ?: defaultValue

/**
 * Returns the [KSClassDeclaration] referenced by the [name] class-valued annotation argument, or
 * `null` when the argument is absent or refers to the "no type" sentinels (`void`/`Unit`).
 */
private fun KSAnnotation.typeDeclaration(name: String): KSClassDeclaration? {
  val declaration = typedArgument<KSType>(name)?.declaration as? KSClassDeclaration ?: return null
  val qualifiedName =
    declaration.qualifiedName?.asString()?.ifBlank { null } ?: declaration.binaryName()
  return when (qualifiedName.lowercase(Locale.ROOT)) {
    "kotlin.unit",
    "java.lang.void",
    "void" -> null
    else -> declaration
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
