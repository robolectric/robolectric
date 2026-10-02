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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Shows how to test foldable postures with Jetpack WindowManager's testing library. */
@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w673dp-h841dp-port-mdpi")
class FoldablePostureTest {
  @get:Rule val publisherRule = WindowLayoutInfoPublisherRule()

  private val collections = mutableListOf<Job>()

  @After
  fun stopCollecting() {
    collections.forEach { it.cancel() }
  }

  @Test
  fun halfOpenedHorizontalFold_isTabletop() {
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
  fun halfOpenedVerticalFold_isBook() {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val layouts = collectWindowLayoutInfo(activity)

    publish(activity, FoldingFeature.State.HALF_OPENED, FoldingFeature.Orientation.VERTICAL)

    val fold = layouts.last().displayFeatures.single() as FoldingFeature
    assertThat(fold.orientation).isEqualTo(FoldingFeature.Orientation.VERTICAL)
    assertThat(fold.isSeparating).isTrue()
    assertThat(fold.bounds.centerX()).isEqualTo(activity.window.decorView.width / 2)
  }

  @Test
  fun flatFold_doesNotSeparateTheWindow() {
    val activity = Robolectric.setupActivity(Activity::class.java)
    val layouts = collectWindowLayoutInfo(activity)

    publish(activity, FoldingFeature.State.FLAT, FoldingFeature.Orientation.HORIZONTAL)

    val fold = layouts.last().displayFeatures.single() as FoldingFeature
    assertThat(fold.state).isEqualTo(FoldingFeature.State.FLAT)
    assertThat(fold.isSeparating).isFalse()
  }

  private fun collectWindowLayoutInfo(activity: Activity): List<WindowLayoutInfo> {
    val layouts = mutableListOf<WindowLayoutInfo>()
    collections +=
      CoroutineScope(Dispatchers.Unconfined).launch {
        WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect {
          layouts.add(it)
        }
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
