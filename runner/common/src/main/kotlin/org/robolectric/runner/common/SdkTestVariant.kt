package org.robolectric.runner.common

import java.lang.reflect.Method
import org.robolectric.pluginapi.Sdk

/**
 * Represents a test variant for a specific SDK version.
 *
 * @property method The test method this variant executes
 * @property sdk The Android SDK version for this variant
 * @property displayName Human-readable name including SDK info
 * @property uniqueId Unique identifier for this variant
 */
@ExperimentalRunnerApi
data class SdkTestVariant(
  val method: Method,
  val sdk: Sdk,
  val displayName: String,
  val uniqueId: String,
) {
  /** The test class containing this method. */
  val testClass: Class<*>
    get() = method.declaringClass

  /** The SDK API level (e.g., 29, 30, 34). */
  val apiLevel: Int
    get() = sdk.apiLevel

  companion object {
    /** Creates a variant for a test method and SDK. */
    @JvmStatic
    fun create(
      method: Method,
      sdk: Sdk,
      baseUniqueId: String,
      alwaysIncludeSdkInName: Boolean = true,
    ): SdkTestVariant {
      val displayName =
        if (alwaysIncludeSdkInName) {
          "${method.name}[sdk=${sdk.apiLevel}]"
        } else {
          method.name
        }
      val uniqueId = "$baseUniqueId/sdk=${sdk.apiLevel}"
      return SdkTestVariant(
        method = method,
        sdk = sdk,
        displayName = displayName,
        uniqueId = uniqueId,
      )
    }
  }
}
