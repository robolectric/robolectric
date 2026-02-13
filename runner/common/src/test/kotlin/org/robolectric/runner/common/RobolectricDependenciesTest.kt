package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.util.Properties
import org.junit.jupiter.api.Test
import org.robolectric.config.AndroidConfigurer
import org.robolectric.internal.SandboxManager
import org.robolectric.internal.bytecode.ClassHandlerBuilder
import org.robolectric.internal.bytecode.ShadowProviders
import org.robolectric.pluginapi.SdkPicker
import org.robolectric.pluginapi.config.ConfigurationStrategy
import org.robolectric.util.inject.Injector

@OptIn(ExperimentalRunnerApi::class)
class RobolectricDependenciesTest {

  @Test
  fun `create returns non-null dependencies`() {
    val deps = RobolectricDependencies.create()

    assertThat(deps.injector).isNotNull()
    assertThat(deps.sandboxManager).isNotNull()
    assertThat(deps.sdkPicker).isNotNull()
    assertThat(deps.configurationStrategy).isNotNull()
    assertThat(deps.androidConfigurer).isNotNull()
    assertThat(deps.shadowProviders).isNotNull()
    assertThat(deps.classHandlerBuilder).isNotNull()
  }

  @Test
  fun `create uses provided properties`() {
    val props = Properties()
    props.setProperty("test.key", "test.value")

    val deps = RobolectricDependencies.create(props)

    assertThat(deps.injector).isNotNull()
  }

  @Test
  fun `dependencies are of correct types`() {
    val deps = RobolectricDependencies.create()

    assertThat(deps.injector).isInstanceOf(Injector::class.java)
    assertThat(deps.sandboxManager).isInstanceOf(SandboxManager::class.java)
    assertThat(deps.sdkPicker).isInstanceOf(SdkPicker::class.java)
    assertThat(deps.configurationStrategy).isInstanceOf(ConfigurationStrategy::class.java)
    assertThat(deps.androidConfigurer).isInstanceOf(AndroidConfigurer::class.java)
    assertThat(deps.shadowProviders).isInstanceOf(ShadowProviders::class.java)
    assertThat(deps.classHandlerBuilder).isInstanceOf(ClassHandlerBuilder::class.java)
  }

  @Test
  fun `multiple calls create independent instances`() {
    val deps1 = RobolectricDependencies.create()
    val deps2 = RobolectricDependencies.create()

    // Instances should be different
    assertThat(deps1).isNotSameInstanceAs(deps2)
    assertThat(deps1.injector).isNotSameInstanceAs(deps2.injector)
  }
}
