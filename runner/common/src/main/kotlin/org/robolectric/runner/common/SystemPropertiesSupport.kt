package org.robolectric.runner.common

/**
 * Utilities for reading and working with Robolectric system properties.
 *
 * This object provides centralized access to Robolectric-specific system properties that control
 * test execution behavior across different test frameworks.
 *
 * ## Supported Properties
 * - `robolectric.alwaysIncludeVariantMarkersInTestName`: When true, test names include SDK markers
 *   even for single-SDK execution (e.g., `testMethod[34]`). Default: false.
 * - `robolectric.enabledSdks`: Comma-separated list of SDK versions to test against (e.g.,
 *   "23,24,25"). Handled by DefaultSdkPicker. When specified, tests execute once per SDK.
 *
 * ## Usage
 *
 * ```kotlin
 * val alwaysShowMarkers = SystemPropertiesSupport.alwaysIncludeVariantMarkersInTestName()
 * val testName = SystemPropertiesSupport.formatTestName("myTest", 34, alwaysShowMarkers, true)
 * // Result: "myTest[34]" if alwaysShowMarkers=true or isLastSdk=false
 * ```
 */
@ExperimentalRunnerApi
object SystemPropertiesSupport {
  private const val ALWAYS_INCLUDE_VARIANT_MARKERS_PROPERTY =
    "robolectric.alwaysIncludeVariantMarkersInTestName"

  /**
   * Returns whether SDK variant markers should always be included in test names.
   *
   * When true, all test names include SDK markers regardless of how many SDKs are configured. When
   * false (default), markers are only included when multiple SDKs are selected.
   *
   * Controlled by `-Drobolectric.alwaysIncludeVariantMarkersInTestName=true` system property.
   *
   * @return true if variant markers should always be included, false otherwise
   */
  @JvmStatic
  fun alwaysIncludeVariantMarkersInTestName(): Boolean {
    return System.getProperty(ALWAYS_INCLUDE_VARIANT_MARKERS_PROPERTY, "false").toBoolean()
  }

  /**
   * Formats a test name with optional SDK marker.
   *
   * The SDK marker is included when:
   * - [alwaysIncludeMarkers] is true, OR
   * - [isLastSdk] is false (indicating multiple SDKs are being tested)
   *
   * This matches the behavior of RobolectricTestRunner, which includes markers for all but the last
   * SDK variant to maintain IDE compatibility while supporting multi-SDK execution.
   *
   * @param baseName The base test method name
   * @param sdkApiLevel The SDK API level for this test variant
   * @param alwaysIncludeMarkers Whether to always include SDK markers
   * @param isLastSdk Whether this is the last (or only) SDK being tested
   * @return The formatted test name, e.g., "testMethod[34]" or "testMethod"
   */
  @JvmStatic
  fun formatTestName(
    baseName: String,
    sdkApiLevel: Int,
    alwaysIncludeMarkers: Boolean,
    isLastSdk: Boolean,
  ): String {
    return if (alwaysIncludeMarkers || !isLastSdk) {
      "$baseName[$sdkApiLevel]"
    } else {
      baseName
    }
  }

  /**
   * Creates a unique identifier segment for an SDK variant.
   *
   * This is used to create unique test descriptor IDs when parameterizing tests across multiple
   * SDKs.
   *
   * @param sdkApiLevel The SDK API level
   * @return A unique identifier segment, e.g., "sdk:34"
   */
  @JvmStatic fun createSdkSegment(sdkApiLevel: Int): String = "sdk:$sdkApiLevel"
}
