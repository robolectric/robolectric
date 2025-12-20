package org.robolectric.junit.jupiter

/**
 * Marks a test to be executed across multiple Android SDK versions.
 *
 * This annotation works in conjunction with the system property `-Drobolectric.enabledSdks` to
 * enable SDK-parameterized test execution. When specified without system properties, tests execute
 * with the default SDK configuration.
 *
 * ## Usage
 *
 * ```kotlin
 * @ExtendWith(RobolectricExtension::class)
 * class MyTest {
 *
 *   @RobolectricSdkTest
 *   fun testAcrossSdks() {
 *     // This test will execute once per SDK when -Drobolectric.enabledSdks is specified
 *     val sdkVersion = Build.VERSION.SDK_INT
 *     // ... test logic
 *   }
 * }
 * ```
 *
 * ## System Properties
 * - `-Drobolectric.enabledSdks=23,24,25`: Execute test across SDK 23, 24, and 25
 * - `-Drobolectric.alwaysIncludeVariantMarkersInTestName=true`: Include SDK markers in test names
 *
 * ## Notes
 * - Without `-Drobolectric.enabledSdks`, tests execute once with the configured SDK from `@Config`
 * - Class-level `@BeforeAll`/`@AfterAll` methods execute once per class, not per SDK
 * - Compatible with `@ParameterizedTest` and other Jupiter annotations
 *
 * @see org.robolectric.annotation.Config
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@org.junit.jupiter.api.TestTemplate
@org.junit.jupiter.api.extension.ExtendWith(
  RobolectricSdkTestTemplateInvocationContextProvider::class
)
annotation class RobolectricSdkTest
