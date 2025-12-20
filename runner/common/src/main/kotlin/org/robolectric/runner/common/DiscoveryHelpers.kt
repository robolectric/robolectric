package org.robolectric.runner.common

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Helper utilities for framework-agnostic test discovery.
 *
 * This object provides methods to:
 * - Discover test methods with custom annotations (works with any test framework)
 * - Discover nested test classes
 * - Create SDK-parameterized test variants
 * - Detect test methods by annotation name (string-based, no compile-time dependencies)
 *
 * ## Framework-Agnostic Discovery
 *
 * ```kotlin
 * // Discover tests with custom annotations
 * val tests = DiscoveryHelpers.discoverTestMethods(
 *   MyTest::class.java,
 *   listOf(Test::class.java, MyCustomTest::class.java),
 * )
 *
 * // With filtering
 * val fastTests = DiscoveryHelpers.discoverTestMethods(
 *   MyTest::class.java,
 *   listOf(Test::class.java),
 *   filter = TestFilter.byMethodName(Regex(".*Fast.*")),
 * )
 * ```
 *
 * ## SDK Parameterization
 *
 * ```kotlin
 * val variants = DiscoveryHelpers.createSdkVariants(testClass, testMethod, deps)
 * variants.forEach { variant ->
 *   executor.executeSandboxedSafe(testClass, testMethod, variant.sdk) { sandbox ->
 *     // Run test for this SDK
 *   }
 * }
 * ```
 */
@ExperimentalRunnerApi
object DiscoveryHelpers {

  // Well-known test annotation class names
  private const val JUNIT_JUPITER_TEST = "org.junit.jupiter.api.Test"
  private const val JUNIT4_TEST = "org.junit.Test"

  /**
   * Determines if a method is a test method by checking for supported @Test annotations.
   *
   * Supports:
   * - JUnit Jupiter: `@org.junit.jupiter.api.Test`
   * - JUnit 4: `@org.junit.Test`
   *
   * @param method The method to check
   * @return true if the method has a recognized test annotation
   */
  @JvmStatic
  fun isTestMethod(method: Method): Boolean {
    return method.annotations.any {
      val name = it.annotationClass.java.name
      name == JUNIT_JUPITER_TEST || name == JUNIT4_TEST
    }
  }

  // ==================== Framework-Agnostic Discovery ====================

  /**
   * Discovers test methods using custom annotation detection.
   *
   * This method scans a test class for methods annotated with any of the provided test annotation
   * classes, optionally including inherited methods.
   *
   * @param testClass The class to scan for test methods
   * @param testAnnotations List of annotation classes that mark test methods
   * @param includeInherited Whether to include test methods from superclasses
   * @param filter Optional filter to exclude certain tests
   * @return List of discovered test methods
   */
  @JvmStatic
  @JvmOverloads
  fun discoverTestMethods(
    testClass: Class<*>,
    testAnnotations: List<Class<out Annotation>>,
    includeInherited: Boolean = true,
    filter: TestFilter = TestFilter.ACCEPT_ALL,
  ): List<Method> {
    val methods =
      if (includeInherited) {
        testClass.methods.toList()
      } else {
        testClass.declaredMethods.toList()
      }

    return methods
      .filter { method ->
        // Check if method has any of the test annotations
        testAnnotations.any { annotation -> method.isAnnotationPresent(annotation) }
      }
      .filter { method ->
        // Apply custom filter
        filter.shouldRun(testClass, method)
      }
      .sortedBy { it.name } // Consistent ordering
  }

  /**
   * Discovers test methods using annotation class names (string-based).
   *
   * This is useful when the annotation class might not be on the classpath.
   *
   * @param testClass The class to scan for test methods
   * @param annotationClassNames Fully qualified class names of test annotations
   * @param includeInherited Whether to include test methods from superclasses
   * @param filter Optional filter to exclude certain tests
   * @return List of discovered test methods
   */
  @JvmStatic
  @JvmOverloads
  fun discoverTestMethodsByName(
    testClass: Class<*>,
    annotationClassNames: List<String>,
    includeInherited: Boolean = true,
    filter: TestFilter = TestFilter.ACCEPT_ALL,
  ): List<Method> {
    val methods =
      if (includeInherited) {
        testClass.methods.toList()
      } else {
        testClass.declaredMethods.toList()
      }

    return methods
      .filter { method ->
        method.annotations.any { annotation ->
          annotationClassNames.contains(annotation.annotationClass.java.name)
        }
      }
      .filter { method -> filter.shouldRun(testClass, method) }
      .sortedBy { it.name }
  }

  /**
   * Discovers nested test classes.
   *
   * @param testClass The class to scan for nested classes
   * @param nestedAnnotation Optional annotation that marks nested test classes (if null, all nested
   *   classes are returned)
   * @return List of nested test classes
   */
  @JvmStatic
  @JvmOverloads
  fun discoverNestedClasses(
    testClass: Class<*>,
    nestedAnnotation: Class<out Annotation>? = null,
  ): List<Class<*>> {
    return testClass.declaredClasses
      .filter { nested ->
        // Exclude static nested classes (they're not inner test classes)
        !Modifier.isStatic(nested.modifiers) || nestedAnnotation != null
      }
      .filter { nested ->
        if (nestedAnnotation != null) {
          nested.isAnnotationPresent(nestedAnnotation)
        } else {
          true
        }
      }
      .sortedBy { it.simpleName }
  }

  /**
   * Creates SDK-parameterized test variants for a test method.
   *
   * When `-Drobolectric.enabledSdks` is set, each test method should be executed once for each
   * enabled SDK. This method creates a variant for each SDK.
   *
   * @param testClass The test class
   * @param testMethod The test method
   * @param deps Robolectric dependencies (for SDK selection)
   * @param baseUniqueId Base unique ID for creating variant IDs
   * @param alwaysIncludeSdkInName Whether to always include SDK in display name
   * @return List of SDK variants for this test
   */
  @JvmStatic
  @JvmOverloads
  fun createSdkVariants(
    testClass: Class<*>,
    testMethod: Method,
    deps: RobolectricDependencies,
    baseUniqueId: String = "${testClass.name}.${testMethod.name}",
    alwaysIncludeSdkInName: Boolean =
      SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName(),
  ): List<SdkTestVariant> {
    // Get configuration for this test
    val configuration = deps.configurationStrategy.getConfig(testClass, testMethod)

    // Resolve manifest
    val config = configuration.get(org.robolectric.annotation.Config::class.java)
    val appManifest = ManifestResolver.resolveManifest(config)

    // Get SDKs after enabledSdks filtering
    val sdks = deps.sdkPicker.selectSdks(configuration, appManifest)

    if (sdks.isEmpty()) {
      return emptyList()
    }

    // Only include SDK in name if there are multiple SDKs or always include is set
    val includeSdkInName = alwaysIncludeSdkInName || sdks.size > 1

    return sdks.map { sdk ->
      SdkTestVariant.create(
        method = testMethod,
        sdk = sdk,
        baseUniqueId = baseUniqueId,
        alwaysIncludeSdkInName = includeSdkInName,
      )
    }
  }

  /**
   * Discovers all test methods in a class and creates SDK variants for each.
   *
   * This is a convenience method that combines [discoverTestMethods] and [createSdkVariants].
   *
   * @param testClass The test class
   * @param testAnnotations Annotations that mark test methods
   * @param deps Robolectric dependencies
   * @param filter Optional filter for tests
   * @return List of all SDK variants for all test methods
   */
  @JvmStatic
  @JvmOverloads
  fun discoverAllVariants(
    testClass: Class<*>,
    testAnnotations: List<Class<out Annotation>>,
    deps: RobolectricDependencies,
    filter: TestFilter = TestFilter.ACCEPT_ALL,
  ): List<SdkTestVariant> {
    val methods = discoverTestMethods(testClass, testAnnotations, filter = filter)
    return methods.flatMap { method -> createSdkVariants(testClass, method, deps) }
  }
}
