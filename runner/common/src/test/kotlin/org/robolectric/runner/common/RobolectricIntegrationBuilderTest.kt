package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [RobolectricIntegrationBuilder]. */
class RobolectricIntegrationBuilderTest {

  @Test
  fun `build creates DefaultRobolectricIntegration`() {
    val integration = RobolectricIntegrationBuilder().build()
    assertThat(integration).isInstanceOf(DefaultRobolectricIntegration::class.java)
  }

  @Test
  fun `default sandbox sharing is PER_CLASS`() {
    val integration = RobolectricIntegrationBuilder().build() as DefaultRobolectricIntegration
    // The default should create a class lifecycle manager internally
    assertThat(integration.getClassLifecycleManager()).isNotNull()
  }

  @Test
  fun `sandboxSharing can be configured`() {
    val integration =
      RobolectricIntegrationBuilder().sandboxSharing(SandboxSharingStrategy.PER_TEST).build()
    assertThat(integration).isInstanceOf(DefaultRobolectricIntegration::class.java)
  }

  @Test
  fun `enableDebugLogging sets debug flag`() {
    val originalValue = RunnerLogger.isDebugEnabled
    try {
      RobolectricIntegrationBuilder().enableDebugLogging().build()
      assertThat(RunnerLogger.isDebugEnabled).isTrue()
    } finally {
      RunnerLogger.isDebugEnabled = originalValue
    }
  }

  @Test
  fun `enableMetrics enables metrics`() {
    val wasEnabled = RunnerMetrics.isEnabled
    try {
      RobolectricIntegrationBuilder().enableMetrics().build()
      assertThat(RunnerMetrics.isEnabled).isTrue()
    } finally {
      if (!wasEnabled) {
        RunnerMetrics.reset()
      }
    }
  }

  @Test
  fun `forJUnitPlatform creates builder with defaults`() {
    val builder = RobolectricIntegrationBuilder.forJUnitPlatform()
    assertThat(builder).isNotNull()
    assertThat(builder.build()).isInstanceOf(DefaultRobolectricIntegration::class.java)
  }

  @Test
  fun `forJUnitJupiter creates builder with defaults`() {
    val builder = RobolectricIntegrationBuilder.forJUnitJupiter()
    assertThat(builder).isNotNull()
    assertThat(builder.build()).isInstanceOf(DefaultRobolectricIntegration::class.java)
  }

  @Test
  fun `parameterResolver can be customized`() {
    val customResolver =
      object : ParameterResolver {
        override fun resolveParameter(
          parameter: java.lang.reflect.Parameter,
          sandbox: org.robolectric.internal.AndroidSandbox,
        ): Any? = null
      }

    val integration =
      RobolectricIntegrationBuilder().parameterResolver(customResolver).build()
        as DefaultRobolectricIntegration

    assertThat(integration.getParameterResolver()).isSameInstanceAs(customResolver)
  }

  @Test
  fun `fromSystemProperties reads debug property`() {
    val originalDebug = System.getProperty("robolectric.runner.debug")
    val originalLoggerValue = RunnerLogger.isDebugEnabled
    try {
      System.setProperty("robolectric.runner.debug", "true")
      val builder = RobolectricIntegrationBuilder.fromSystemProperties()
      builder.build()
      // The builder should have read the property and enabled debug
      assertThat(RunnerLogger.isDebugEnabled).isTrue()
    } finally {
      if (originalDebug != null) {
        System.setProperty("robolectric.runner.debug", originalDebug)
      } else {
        System.clearProperty("robolectric.runner.debug")
      }
      RunnerLogger.isDebugEnabled = originalLoggerValue
    }
  }
}
