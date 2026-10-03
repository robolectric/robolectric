package org.robolectric.integrationtests.androidx

import android.app.Activity
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

/** What Jetpack WindowManager reports under Robolectric when nothing is simulated. */
@RunWith(AndroidJUnit4::class)
class JetpackWindowManagerTest {
  private val activity: Activity = Robolectric.setupActivity(Activity::class.java)

  @Test
  fun windowLayoutInfo_hasNoFolds() {
    val layouts = mutableListOf<WindowLayoutInfo>()
    val collection =
      CoroutineScope(Dispatchers.Unconfined).launch {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect {
          layouts.add(it)
        }
      }
    shadowOf(Looper.getMainLooper()).idle()
    collection.cancel()

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
