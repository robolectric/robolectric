package org.robolectric.integrationtests.androidx

import android.app.Activity
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import androidx.window.testing.layout.FoldingFeature as TestFoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Shows how to test foldable postures with Jetpack WindowManager's testing library. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w673dp-h841dp-port-mdpi")
class FoldablePostureTest {
  @get:Rule val publisherRule = WindowLayoutInfoPublisherRule()

  @Test
  fun halfOpenedHorizontalFold_isTabletop() = runTest {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val layouts = collectWindowLayoutInfo(activity)

    publish(activity, FoldingFeature.State.HALF_OPENED, FoldingFeature.Orientation.HORIZONTAL)

    val fold = layouts.last().displayFeatures.single() as FoldingFeature
    assertThat(fold.state).isEqualTo(FoldingFeature.State.HALF_OPENED)
    assertThat(fold.orientation).isEqualTo(FoldingFeature.Orientation.HORIZONTAL)
    assertThat(fold.isSeparating).isTrue()
    assertThat(fold.bounds.centerY()).isEqualTo(activity.window.decorView.height / 2)
  }

  @Test
  fun halfOpenedVerticalFold_isBook() = runTest {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val layouts = collectWindowLayoutInfo(activity)

    publish(activity, FoldingFeature.State.HALF_OPENED, FoldingFeature.Orientation.VERTICAL)

    val fold = layouts.last().displayFeatures.single() as FoldingFeature
    assertThat(fold.orientation).isEqualTo(FoldingFeature.Orientation.VERTICAL)
    assertThat(fold.isSeparating).isTrue()
    assertThat(fold.bounds.centerX()).isEqualTo(activity.window.decorView.width / 2)
  }

  @Test
  fun flatFold_doesNotSeparateTheWindow() = runTest {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val layouts = collectWindowLayoutInfo(activity)

    publish(activity, FoldingFeature.State.FLAT, FoldingFeature.Orientation.HORIZONTAL)

    val fold = layouts.last().displayFeatures.single() as FoldingFeature
    assertThat(fold.state).isEqualTo(FoldingFeature.State.FLAT)
    assertThat(fold.isSeparating).isFalse()
  }

  /** Collects the layouts of the activity's window until the test is over. */
  private fun TestScope.collectWindowLayoutInfo(activity: Activity): List<WindowLayoutInfo> {
    val layouts = mutableListOf<WindowLayoutInfo>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect { layouts.add(it) }
    }
    return layouts
  }

  private fun publish(
    activity: Activity,
    state: FoldingFeature.State,
    orientation: FoldingFeature.Orientation,
  ) {
    publisherRule.overrideWindowLayoutInfo(
      TestWindowLayoutInfo(
        listOf(TestFoldingFeature(activity = activity, state = state, orientation = orientation))
      )
    )
    shadowOf(Looper.getMainLooper()).idle()
  }
}
