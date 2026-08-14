package org.robolectric.junit.jupiter

import java.lang.reflect.Method
import org.junit.jupiter.api.Nested
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.robolectric.pluginapi.Sdk
import org.robolectric.runner.common.DiscoveryHelpers
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricDependencies
import org.robolectric.runner.common.SystemPropertiesSupport

/**
 * JUnit Platform descriptor builders for JUnit Jupiter tests.
 *
 * This object provides methods to build JUnit Platform test descriptor trees specifically for JUnit
 * Jupiter tests, including support for `@Nested` test classes and per-SDK variant descriptors
 * (mirroring `PlatformDescriptorBuilders` in `:runner:junit-platform`).
 */
@OptIn(ExperimentalRunnerApi::class)
internal object JupiterDescriptorBuilders {

  /**
   * Appends a test class and its test methods to the parent descriptor.
   *
   * This method recursively handles nested classes annotated with @Nested.
   *
   * @param parent The parent test descriptor
   * @param testClass The test class to append
   * @param isTestMethod A predicate to determine if a method is a test method
   * @param nestedAnnotation The annotation class used to identify nested classes (e.g.
   *   org.junit.jupiter.api.Nested)
   * @param unsupportedKind Classifier returning a label (e.g. "@TestTemplate") for test-method
   *   kinds the engine cannot execute. Such methods are still discovered — as
   *   [UnsupportedJupiterMethodDescriptor]s that fail loudly at execution time — so they can never
   *   vanish silently from a run.
   * @param sdkVariantDeps When non-null, methods whose configuration selects multiple SDKs (via
   *   `@Config(sdk = …)` and `-Drobolectric.enabledSdks`) are expanded into one
   *   [JupiterSdkMethodDescriptor] per SDK, mirroring the Platform engine's parameterization.
   */
  @Suppress("LongParameterList")
  fun appendTestClass(
    parent: TestDescriptor,
    testClass: Class<*>,
    isTestMethod: (Method) -> Boolean,
    nestedAnnotation: Class<out Annotation>? = Nested::class.java,
    unsupportedKind: (Method) -> String? = { null },
    sdkVariantDeps: RobolectricDependencies? = null,
  ) {
    val classId = parent.uniqueId.append("class", testClass.name)
    val classDescriptor =
      parent.children.find { it.uniqueId == classId }
        ?: JupiterClassDescriptor(classId, testClass).also { parent.addChild(it) }

    // Add test methods directly to this class descriptor
    testClass.declaredMethods.forEach { method ->
      val kindLabel = unsupportedKind(method)
      if (kindLabel != null) {
        val methodId = classDescriptor.uniqueId.append("unsupported", method.name)
        if (classDescriptor.children.none { it.uniqueId == methodId }) {
          classDescriptor.addChild(UnsupportedJupiterMethodDescriptor(methodId, method, kindLabel))
        }
      } else if (isTestMethod(method)) {
        appendMethodVariants(classDescriptor, testClass, method, sdkVariantDeps)
      }
    }

    // Recursively add nested classes if annotation is provided
    if (nestedAnnotation != null) {
      testClass.declaredClasses
        .filter { AnnotationSupport.isAnnotated(it, nestedAnnotation) }
        .forEach { nestedClass ->
          appendTestClass(
            classDescriptor,
            nestedClass,
            isTestMethod,
            nestedAnnotation,
            unsupportedKind,
            sdkVariantDeps,
          )
        }
    }
  }

  /**
   * Emits one descriptor per selected SDK when the method's configuration yields multiple SDKs, or
   * a single plain [JupiterMethodDescriptor] otherwise.
   */
  private fun appendMethodVariants(
    classDescriptor: TestDescriptor,
    testClass: Class<*>,
    method: Method,
    sdkVariantDeps: RobolectricDependencies?,
  ) {
    val variants =
      if (sdkVariantDeps != null) {
        DiscoveryHelpers.createSdkVariants(testClass, method, sdkVariantDeps)
      } else {
        emptyList()
      }

    if (variants.size <= 1) {
      val methodId = classDescriptor.uniqueId.append("method", method.name)
      if (classDescriptor.children.none { it.uniqueId == methodId }) {
        classDescriptor.addChild(JupiterMethodDescriptor(methodId, method))
      }
      return
    }

    val alwaysIncludeMarkers = SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()
    variants.forEachIndexed { index, variant ->
      val isLastSdk = index == variants.size - 1
      val displayName =
        SystemPropertiesSupport.formatTestName(
          method.name,
          variant.sdk.apiLevel,
          alwaysIncludeMarkers,
          isLastSdk,
        )
      val sdkSegment = SystemPropertiesSupport.createSdkSegment(variant.sdk.apiLevel)
      val methodId = classDescriptor.uniqueId.append("method", "${method.name}[$sdkSegment]")
      if (classDescriptor.children.none { it.uniqueId == methodId }) {
        classDescriptor.addChild(
          JupiterSdkMethodDescriptor(methodId, method, variant.sdk, displayName)
        )
      }
    }
  }

  /**
   * Appends a test method to the parent descriptor.
   *
   * @param parent The parent test descriptor
   * @param testClass The class containing the method
   * @param method The test method to append
   */
  fun appendTestMethod(parent: TestDescriptor, testClass: Class<*>, method: Method) {
    val classDescriptor = findOrCreateClassDescriptor(parent, testClass)
    val methodId = classDescriptor.uniqueId.append("method", method.name)
    if (classDescriptor.children.none { it.uniqueId == methodId }) {
      classDescriptor.addChild(JupiterMethodDescriptor(methodId, method))
    }
  }

  // Trivial helper duplicated in PlatformDescriptorBuilders — cannot live in :runner:common
  // because that module intentionally has no junit-platform compile dependency. Keep the two
  // copies in sync.
  private fun findOrCreateClassDescriptor(
    parent: TestDescriptor,
    testClass: Class<*>,
  ): TestDescriptor {
    val classId = parent.uniqueId.append("class", testClass.name)
    return parent.children.find { it.uniqueId == classId }
      ?: JupiterClassDescriptor(classId, testClass).also { parent.addChild(it) }
  }

  /** Descriptor representing a test class. */
  class JupiterClassDescriptor(uniqueId: UniqueId, val testClass: Class<*>) :
    AbstractTestDescriptor(uniqueId, testClass.simpleName, ClassSource.from(testClass)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER
  }

  /** Descriptor representing a single test method executed on one resolved SDK. */
  class JupiterMethodDescriptor(uniqueId: UniqueId, val method: Method) :
    AbstractTestDescriptor(uniqueId, method.name, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }

  /** Descriptor for one per-SDK variant of a test method (multi-SDK discovery). */
  class JupiterSdkMethodDescriptor(
    uniqueId: UniqueId,
    val method: Method,
    val sdk: Sdk,
    displayName: String,
  ) : AbstractTestDescriptor(uniqueId, displayName, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }

  /**
   * Descriptor for a test-method kind this engine cannot execute (e.g. `@TestTemplate`,
   * `@TestFactory`). Discovered so the method cannot silently vanish; execution reports it as
   * failed with an actionable message.
   */
  class UnsupportedJupiterMethodDescriptor(
    uniqueId: UniqueId,
    val method: Method,
    val kindLabel: String,
  ) : AbstractTestDescriptor(uniqueId, method.name, MethodSource.from(method)) {
    override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST
  }
}
