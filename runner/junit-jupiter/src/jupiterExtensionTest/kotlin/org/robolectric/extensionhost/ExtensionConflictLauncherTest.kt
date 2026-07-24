package org.robolectric.extensionhost

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.EngineFilter
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension

private const val FIXTURE_PROPERTY = "org.robolectric.extensionhost.conflictFixture"

/**
 * Verifies fail-fast conflict semantics of [RobolectricExtension] on the standard Jupiter engine: a
 * plain method-level `@Config` SDK override on a class that declares `@BeforeAll` must fail with
 * the canonical conflict message instead of being silently ignored.
 *
 * The fixture is guarded by a system property so neither test task discovers it directly; this test
 * launches it through a nested JUnit Platform launcher run and asserts on the summary.
 */
class ExtensionConflictLauncherTest {

  @Test
  fun methodSdkOverrideWithClassLifecycleFailsFast() {
    System.setProperty(FIXTURE_PROPERTY, "true")
    try {
      val listener = SummaryGeneratingListener()
      val request =
        LauncherDiscoveryRequestBuilder.request()
          .selectors(DiscoverySelectors.selectClass(ExtensionConflictFixture::class.java))
          .filters(EngineFilter.includeEngines("junit-jupiter"))
          .build()
      LauncherFactory.create().execute(request, listener)

      val summary = listener.summary
      assertThat(summary.testsSucceededCount).isEqualTo(1)
      assertThat(summary.testsFailedCount).isEqualTo(1)
      val failureMessage = summary.failures.single().exception.message
      assertThat(failureMessage).contains("Configuration conflict")
      assertThat(failureMessage).contains("@RobolectricSdkTest")
    } finally {
      System.clearProperty(FIXTURE_PROPERTY)
    }
  }
}

@EnabledIfSystemProperty(named = FIXTURE_PROPERTY, matches = "true")
@ExtendWith(RobolectricExtension::class)
@Config(sdk = [33])
class ExtensionConflictFixture {

  companion object {
    @JvmStatic @BeforeAll fun setUpClass() = Unit
  }

  @Test
  fun matchingMethodSharesClassEnvironment() {
    assertThat(Build.VERSION.SDK_INT).isEqualTo(33)
  }

  @Test
  @Config(sdk = [34])
  fun conflictingMethodFailsFast() {
    // Never reached: the extension fails this test before invoking it.
    error("should not execute")
  }
}
