package org.robolectric.integrationtests.androidx

import android.app.Activity
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

/** What Jetpack WindowManager reports under Robolectric when nothing is simulated. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class JetpackWindowManagerTest {
  private val activity: Activity = Robolectric.setupActivity(Activity::class.java)

  @Test
  fun windowLayoutInfo_hasNoFolds() = runTest {
    val layouts = mutableListOf<WindowLayoutInfo>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect { layouts.add(it) }
    }
    shadowOf(Looper.getMainLooper()).idle()

    assertThat(layouts.single().displayFeatures).isEmpty()
  }

  @Test
  fun activityEmbedding_isUnavailable() {
    assertThat(SplitController.getInstance(activity).splitSupportStatus)
      .isEqualTo(SplitController.SplitSupportStatus.SPLIT_UNAVAILABLE)
    assertThat(ActivityEmbeddingController.getInstance(activity).isActivityEmbedded(activity))
      .isFalse()
  }
}
