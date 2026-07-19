package org.robolectric.activityscenario

import androidx.test.core.app.ActivityScenario
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.robolectric.annotation.Config
import org.robolectric.testhelpers.ConfigTestActivity

@Config(sdk = [34], manifest = "AndroidManifest.xml")
class ActivityScenarioEngineTest {

  @Test
  fun launch_withoutManifestEntryFailsWithActionableMessage() {
    val error =
      assertThrows(RuntimeException::class.java) {
        ActivityScenario.launch(ConfigTestActivity::class.java)
      }
    assertThat(error).hasMessageThat().contains("Unable to resolve activity for Intent")
  }

  @Test
  fun launch_errorMentionsDiagnosticReference() {
    val error =
      assertThrows(RuntimeException::class.java) {
        ActivityScenario.launch(ConfigTestActivity::class.java)
      }
    assertThat(error).hasMessageThat().contains("robolectric/pull/4736")
  }
}
