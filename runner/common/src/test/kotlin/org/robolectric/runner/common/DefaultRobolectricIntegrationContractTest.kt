package org.robolectric.runner.common
/** Contract test for [DefaultRobolectricIntegration]. */
@ExperimentalRunnerApi
class DefaultRobolectricIntegrationContractTest : RobolectricIntegrationContractTest() {
  override fun createIntegration(): RobolectricIntegration {
    return RobolectricIntegrationBuilder.forJUnitPlatform().build()
  }
}
