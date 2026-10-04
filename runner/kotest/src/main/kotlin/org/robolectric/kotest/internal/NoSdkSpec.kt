package org.robolectric.kotest.internal

import io.kotest.core.spec.style.FunSpec

/**
 * Stands in for a spec that no Android SDK is selected for, which is then not created. Its only
 * test is disabled, and named after the reason, because Kotest doesn't report why the tests of a
 * spec are disabled if all of them are.
 */
internal class NoSdkSpec(specClass: Class<*>) : FunSpec({ xtest(reason(specClass)) {} }) {
  private companion object {
    fun reason(specClass: Class<*>): String {
      val enabledSdks = System.getProperty("robolectric.enabledSdks")
      return if (enabledSdks.isNullOrBlank()) {
        "None of the Android SDKs that ${specClass.name} is configured for can run on this JVM"
      } else {
        "None of the Android SDKs that ${specClass.name} is configured for is enabled by " +
          "robolectric.enabledSdks=$enabledSdks"
      }
    }
  }
}
