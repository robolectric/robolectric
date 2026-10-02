package org.robolectric.integrationtests.androidx

import android.app.Activity
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.embedding.ActivityEmbeddingController
import androidx.window.embedding.SplitController
import androidx.window.embedding.SplitInfo
import androidx.window.testing.embedding.ActivityEmbeddingRule
import androidx.window.testing.embedding.TestActivityStack
import androidx.window.testing.embedding.TestSplitInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

/** Shows how to test activity embedding with Jetpack WindowManager's testing library. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ActivityEmbeddingTest {
  @get:Rule val embeddingRule = ActivityEmbeddingRule()

  @Test
  fun activity_canBeEmbedded() {
    val activity = Robolectric.setupActivity(Activity::class.java)

    embeddingRule.overrideIsActivityEmbedded(activity, true)

    assertThat(ActivityEmbeddingController.getInstance(activity).isActivityEmbedded(activity))
      .isTrue()
  }

  @Test
  fun split_isReportedToTheActivitiesInIt() = runTest {
    val primary = Robolectric.setupActivity(Activity::class.java)
    val secondary = Robolectric.setupActivity(Activity::class.java)
    embeddingRule.overrideSplitSupportStatus(SplitController.SplitSupportStatus.SPLIT_AVAILABLE)
    val splits = mutableListOf<List<SplitInfo>>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      SplitController.getInstance(primary).splitInfoList(primary).collect { splits.add(it) }
    }

    embeddingRule.overrideSplitInfo(
      primary,
      listOf(
        TestSplitInfo(TestActivityStack(listOf(primary)), TestActivityStack(listOf(secondary)))
      ),
    )
    shadowOf(Looper.getMainLooper()).idle()

    val split = splits.last().single()
    assertThat(SplitController.getInstance(primary).splitSupportStatus)
      .isEqualTo(SplitController.SplitSupportStatus.SPLIT_AVAILABLE)
    assertThat(primary in split.primaryActivityStack).isTrue()
    assertThat(secondary in split.secondaryActivityStack).isTrue()
  }
}
