package org.robolectric.manifest

import com.google.common.truth.Truth.assertThat
import java.util.ArrayList
import java.util.HashMap
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ActivityDataTest {
  @Test
  fun test_non_android_namespace() {
    val attrs = HashMap<String, String>()
    attrs["testns:name"] = ".test.TestActivity"
    val activityData = ActivityData("testns", attrs, ArrayList())

    assertThat(activityData.name).isEqualTo(".test.TestActivity")
    assertThat(activityData.allAttributes["android:name"]).isNull()
  }

  @Test
  fun test_config_changes() {
    val attrs = HashMap<String, String>()
    attrs["android:configChanges"] = "mcc|screenLayout|orientation"
    val activityData = ActivityData(attrs, ArrayList())

    assertThat(activityData.configChanges).isEqualTo("mcc|screenLayout|orientation")
  }
}
