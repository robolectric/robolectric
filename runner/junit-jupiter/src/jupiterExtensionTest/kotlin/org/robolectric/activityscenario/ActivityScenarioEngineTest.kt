package org.robolectric.activityscenario

import androidx.test.core.app.ActivityScenario
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.testhelpers.ConfigTestActivity

@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34], manifest = "AndroidManifest.xml")
class ActivityScenarioEngineTest {

  @Test
  fun launch_withoutManifestEntryFailsWithActionableMessage() {
    val error =
      assertThrows<RuntimeException> { ActivityScenario.launch(ConfigTestActivity::class.java) }
    assertThat(error).hasMessageThat().contains("Unable to resolve activity for Intent")
  }

  @Test
  fun launch_errorMentionsDiagnosticReference() {
    val error =
      assertThrows<RuntimeException> { ActivityScenario.launch(ConfigTestActivity::class.java) }
    assertThat(error).hasMessageThat().contains("robolectric/pull/4736")
  }
}
