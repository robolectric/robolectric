@file:Suppress("TooManyFunctions", "SpreadOperator")

package org.robolectric.annotation.processing.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

private const val GEN_CLASS = "Shadows"
private const val IMPLEMENTS_ANNOTATION = "org.robolectric.annotation.Implements"
private const val RESETTER_ANNOTATION = "org.robolectric.annotation.Resetter"
private const val JVM_STATIC_ANNOTATION = "kotlin.jvm.JvmStatic"
private const val DEFAULT_SHADOW_PICKER =
  "org.robolectric.annotation.Implements.DefaultShadowPicker"
private const val DEFAULT_SHADOW_PICKER_BINARY =
  "org.robolectric.annotation.Implements\$DefaultShadowPicker"

private const val PACKAGE_OPT = "org.robolectric.annotation.processing.shadowPackage"
private const val SHOULD_INSTRUMENT_PKG_OPT =
  "org.robolectric.annotation.processing.shouldInstrumentPackage"
private const val PRIORITY_OPT = "org.robolectric.annotation.processing.priority"

class RobolectricKspProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return RobolectricKspProcessor(environment)
  }
}

private class RobolectricKspProcessor(private val environment: SymbolProcessorEnvironment) :
  SymbolProcessor {
  private var generated = false

  @Suppress("ReturnCount")
  override fun process(resolver: Resolver): List<KSAnnotated> {
    if (generated) {
      return emptyList()
    }

    val shadowPackage = environment.options[PACKAGE_OPT]
    if (shadowPackage.isNullOrBlank()) {
      environment.logger.error("no package specified for $PACKAGE_OPT")
      generated = true
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
      val shadowInfo = declaration.toShadowInfo(environment.logger) ?: return@forEach
      shadowInfos += shadowInfo
      collectResetters(declaration, shadowInfo, resetterInfos, sourceFiles)
    }

    if (shadowInfos.isEmpty()) {
      generated = true
      return emptyList()
    }

    writeShadowProvider(
      codeGenerator = environment.codeGenerator,
      dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray()),
      shadowPackage = shadowPackage,
      shouldInstrumentPackages = shouldInstrumentPackages,
      priority = priority,
      shadowInfos = shadowInfos,
      resetterInfos = resetterInfos,
    )
    writeServiceFile(
      codeGenerator = environment.codeGenerator,
      dependencies = Dependencies(aggregating = true, *sourceFiles.toTypedArray()),
      shadowPackage = shadowPackage,
    )

    generated = true
    return emptyList()
  }
}

private data class ShadowInfo(
  val actualName: String,
  val shadowBinaryName: String,
  val shadowPickerBinaryName: String?,
  val minSdk: Int,
  val maxSdk: Int,
)

private data class ResetterInfo(val methodCall: String, val minSdk: Int, val maxSdk: Int)

private fun collectResetters(
  declaration: KSClassDeclaration,
  shadowInfo: ShadowInfo,
  resetterInfos: MutableList<ResetterInfo>,
  sourceFiles: MutableSet<KSFile>,
) {
  declaration
    .getDeclaredFunctions()
    .filter { it.hasAnnotation(RESETTER_ANNOTATION) }
    .forEach { function ->
      function.containingFile?.let(sourceFiles::add)
      if (function.modifiers.contains(Modifier.JAVA_STATIC)) {
        resetterInfos +=
          ResetterInfo(
            methodCall = "${shadowInfo.shadowBinaryName}.${function.simpleName.asString()}();",
            minSdk = shadowInfo.minSdk,
            maxSdk = shadowInfo.maxSdk,
          )
      }
    }

  declaration.declarations.filterIsInstance<KSClassDeclaration>().forEach { nestedDeclaration ->
    if (!nestedDeclaration.isCompanionObject) {
      return@forEach
    }
    nestedDeclaration
      .getDeclaredFunctions()
      .filter { it.hasAnnotation(RESETTER_ANNOTATION) && it.hasAnnotation(JVM_STATIC_ANNOTATION) }
      .forEach { function ->
        function.containingFile?.let(sourceFiles::add)
        resetterInfos +=
          ResetterInfo(
            methodCall = "${shadowInfo.shadowBinaryName}.${function.simpleName.asString()}();",
            minSdk = shadowInfo.minSdk,
            maxSdk = shadowInfo.maxSdk,
          )
      }
  }
}

@Suppress("ReturnCount")
private fun KSClassDeclaration.toShadowInfo(logger: KSPLogger): ShadowInfo? {
  val implementsAnnotation = findAnnotation(IMPLEMENTS_ANNOTATION) ?: return null
  val className = implementsAnnotation.stringArgument("className")
  val valueClassName = implementsAnnotation.typeArgument("value")
  val actualName =
    if (className.isNotBlank()) {
      className
    } else {
      valueClassName
    }
  if (actualName.isNullOrBlank()) {
    logger.error("Unable to resolve @Implements target for ${qualifiedName?.asString()}")
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
    minSdk = implementsAnnotation.intArgument("minSdk", -1),
    maxSdk = implementsAnnotation.intArgument("maxSdk", -1),
  )
}

@Suppress("LongParameterList", "LongMethod")
private fun writeShadowProvider(
  codeGenerator: CodeGenerator,
  dependencies: Dependencies,
  shadowPackage: String,
  shouldInstrumentPackages: Boolean,
  priority: Int,
  shadowInfos: List<ShadowInfo>,
  resetterInfos: List<ResetterInfo>,
) {
  val shadowsByActual =
    shadowInfos.sortedWith(compareBy(ShadowInfo::actualName, ShadowInfo::shadowBinaryName))
  val shadowPickers = shadowsByActual.filter { it.shadowPickerBinaryName != null }
  val directShadows = shadowsByActual.filter { it.shadowPickerBinaryName == null }
  val providedPackages =
    if (!shouldInstrumentPackages) {
      emptyList()
    } else {
      directShadows
        .mapNotNull {
          it.actualName.substringBeforeLast('.', missingDelimiterValue = "").ifBlank { null }
        }
        .distinct()
        .sorted()
    }

  codeGenerator
    .createNewFile(
      dependencies = dependencies,
      packageName = shadowPackage,
      fileName = GEN_CLASS,
      extensionName = "java",
    )
    .use { output ->
      output.writeText("package $shadowPackage;\n\n")
      output.writeText("import java.util.AbstractMap;\n")
      output.writeText("import java.util.ArrayList;\n")
      output.writeText("import java.util.Collection;\n")
      output.writeText("import java.util.HashMap;\n")
      output.writeText("import java.util.List;\n")
      output.writeText("import java.util.Map;\n")
      output.writeText("import org.robolectric.internal.ShadowProvider;\n\n")
      output.writeText("/** Shadow mapper generated by Robolectric KSP processor. */\n")
      if (priority != 0) {
        output.writeText("@javax.annotation.Priority($priority)\n")
      }
      output.writeText("@SuppressWarnings({\"unchecked\", \"deprecation\"})\n")
      output.writeText("public class $GEN_CLASS implements ShadowProvider {\n")
      output.writeText(
        "  private static final List<Map.Entry<String, String>> SHADOWS = new ArrayList<>(${directShadows.size});\n"
      )
      output.writeText(
        "  private static final Map<String, String> SHADOW_PICKER_MAP = new HashMap<>(${shadowPickers.size});\n\n"
      )
      output.writeText("  static {\n")
      directShadows.forEach { shadowInfo ->
        val entryLine =
          "    SHADOWS.add(new AbstractMap.SimpleImmutableEntry<>(" +
            "\"${shadowInfo.actualName}\", \"${shadowInfo.shadowBinaryName}\"));\n"
        output.writeText(entryLine)
      }
      shadowPickers.forEach { shadowInfo ->
        output.writeText(
          "    SHADOW_PICKER_MAP.put(\"${shadowInfo.actualName}\", \"${shadowInfo.shadowPickerBinaryName}\");\n"
        )
      }
      output.writeText("  }\n\n")

      output.writeText("  @Override\n")
      output.writeText("  public void reset() {\n")
      resetterInfos.forEach { resetter ->
        val guard =
          when {
            resetter.minSdk != -1 && resetter.maxSdk != -1 ->
              "if (org.robolectric.RuntimeEnvironment.getApiLevel() >= ${resetter.minSdk} " +
                "&& org.robolectric.RuntimeEnvironment.getApiLevel() <= ${resetter.maxSdk}) "
            resetter.minSdk != -1 ->
              "if (org.robolectric.RuntimeEnvironment.getApiLevel() >= ${resetter.minSdk}) "
            resetter.maxSdk != -1 ->
              "if (org.robolectric.RuntimeEnvironment.getApiLevel() <= ${resetter.maxSdk}) "
            else -> ""
          }
        output.writeText("    $guard${resetter.methodCall}\n")
      }
      output.writeText("  }\n\n")

      output.writeText("  @Override\n")
      output.writeText("  public Collection<Map.Entry<String, String>> getShadows() {\n")
      output.writeText("    return SHADOWS;\n")
      output.writeText("  }\n\n")

      output.writeText("  @Override\n")
      output.writeText("  public String[] getProvidedPackageNames() {\n")
      if (providedPackages.isEmpty()) {
        output.writeText("    return new String[] {};\n")
      } else {
        output.writeText("    return new String[] {\n")
        providedPackages.forEachIndexed { index, packageName ->
          val suffix = if (index == providedPackages.lastIndex) "" else ","
          output.writeText("      \"$packageName\"$suffix\n")
        }
        output.writeText("    };\n")
      }
      output.writeText("  }\n\n")

      output.writeText("  @Override\n")
      output.writeText("  public Map<String, String> getShadowPickerMap() {\n")
      output.writeText("    return SHADOW_PICKER_MAP;\n")
      output.writeText("  }\n")
      output.writeText("}\n")
    }
}

private fun writeServiceFile(
  codeGenerator: CodeGenerator,
  dependencies: Dependencies,
  shadowPackage: String,
) {
  codeGenerator
    .createNewFileByPath(
      dependencies = dependencies,
      path = "META-INF/services/org.robolectric.internal.ShadowProvider",
      extensionName = "",
    )
    .use { output -> output.writeText("$shadowPackage.$GEN_CLASS\n") }
}

private fun KSAnnotated.hasAnnotation(annotationName: String): Boolean =
  findAnnotation(annotationName) != null

private fun KSAnnotated.findAnnotation(annotationName: String): KSAnnotation? =
  annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationName
  }

private fun KSAnnotation.stringArgument(name: String): String {
  return arguments.firstOrNull { it.name?.asString() == name }?.value as? String ?: ""
}

private fun KSAnnotation.intArgument(name: String, defaultValue: Int): Int {
  return (arguments.firstOrNull { it.name?.asString() == name }?.value as? Int) ?: defaultValue
}

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

private fun OutputStream.writeText(value: String) {
  write(value.toByteArray(StandardCharsets.UTF_8))
}
