package org.robolectric.integration.roborazzi

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.github.takahirom.roborazzi.roboOutputName
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Integration Test for Roborazzi
 *
 * This test is not intended to obstruct the release of Robolectric. In the event that issues are
 * detected which do not stem from Robolectric, the test can be temporarily disabled, and an issue
 * can be reported on the Roborazzi repository.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [S])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class RoborazziCaptureTest {
  @Test
  fun checkViewWithElevationRendering() {
    hardwareRendererEnvironment {
      registerActivityToPackageManager(RoborazziViewWithElevationTestActivity::class.java.name)
      val activityScenario =
        ActivityScenario.launch(RoborazziViewWithElevationTestActivity::class.java)

      captureScreenWithRoborazzi()

      // View the screenshots at build/output/roborazzi/*.png
      val bitmap =
        BitmapFactory.decodeFile("${DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH}/${roboOutputName()}.png")
      activityScenario.onActivity { activity ->
        val locationOnScreen = IntArray(2)
        activity.view?.getLocationOnScreen(locationOnScreen)
        // Check the background color of the FrameLayout
        bitmap.getPixel(locationOnScreen[0] + 1, locationOnScreen[1] + 1).let {
          assertThat(it).isEqualTo(activity.expectedViewBackgroundColor)
        }
      }
    }
  }

  @Test
  fun checkDialogRendering() {
    hardwareRendererEnvironment {
      registerActivityToPackageManager(RoborazziDialogTestActivity::class.java.name)
      val activityScenario = ActivityScenario.launch(RoborazziDialogTestActivity::class.java)

      captureScreenWithRoborazzi()

      // View the screenshots at build/output/roborazzi/*.png
      val bitmap =
        BitmapFactory.decodeFile("${DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH}/${roboOutputName()}.png")
      activityScenario.onActivity { activity ->
        val dialogContentView = activity.dialogContentView
        val (left, top) = dialogContentView.getLocationInScreen(activity)
        bitmap.getPixel(left + 1, top + 1).let {
          assertThat(it).isEqualTo(activity.expectedDialogContentBackgroundColor)
        }
      }
    }
  }

  private fun captureScreenWithRoborazzi() {
    captureScreenRoboImage(
      roborazziOptions =
        RoborazziOptions(
          // roborazziOptions.taskType can modify the behavior.
          // The default is RoborazziTaskType.None, meaning no screenshot capture.
          // Normally, setting roborazziOptions.taskType to RoborazziTaskType.Record is unnecessary,
          // as the Roborazzi Plugin automatically configures it.
          // However, for this test case, we want to bypass the Roborazzi Plugin,
          // focusing solely on the screenshot capture.
          // Hence, we set roborazziOptions.taskType to RoborazziTaskType.Record.
          taskType = RoborazziTaskType.Record
        )
    )
  }

  companion object {
    const val USE_HARDWARE_RENDERER_NATIVE_ENV = "robolectric.screenshot.hwrdr.native"
  }
}

private fun View?.getLocationInScreen(activity: RoborazziDialogTestActivity): Pair<Int, Int> {
  val viewLocationOnWindow = IntArray(2)
  // We can't get the location of the dialogContentView so we use the location in the window
  // and calculate the location of the dialogContentView.
  // This will return wrong value activity.dialogContentView?.getLocationOnScreen(locationOnScreen)
  this?.getLocationInWindow(viewLocationOnWindow)
  val windowLocationOnScreen = Rect()
  val layoutParams = (this?.rootView?.layoutParams as WindowManager.LayoutParams)
  Gravity.apply(
    layoutParams.gravity,
    this.rootView?.width ?: 0,
    this.rootView?.height ?: 0,
    Rect(0, 0, activity.window?.decorView?.width ?: 0, activity.window?.decorView?.height ?: 0),
    windowLocationOnScreen
  )
  // Check the background color of the FrameLayout
  val left = windowLocationOnScreen.left + viewLocationOnWindow[0]
  val top = windowLocationOnScreen.top + viewLocationOnWindow[1]
  return Pair(left, top)
}

private fun registerActivityToPackageManager(activity: String) {
  val appContext: Application =
    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
  Shadows.shadowOf(appContext.packageManager)
    .addActivityIfNotPresent(
      ComponentName(
        appContext.packageName,
        activity,
      )
    )
}

private fun hardwareRendererEnvironment(block: () -> Unit) {
  val originalHwrdrOption =
    System.getProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV, null)
  // This cause ClassNotFoundException: java.nio.NioUtils
  // System.setProperty(USE_HARDWARE_RENDERER_NATIVE_ENV, "true")
  try {
    block()
  } finally {
    if (originalHwrdrOption == null) {
      System.clearProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV)
    } else {
      System.setProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV, originalHwrdrOption)
    }
  }
}

private class RoborazziViewWithElevationTestActivity : Activity() {

  var view: FrameLayout? = null
  val expectedViewBackgroundColor: Int = Color.MAGENTA
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(android.R.style.Theme_Light_NoTitleBar)
    super.onCreate(savedInstanceState)
    setContentView(
      LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        fun Int.toDp(): Int = (this * resources.displayMetrics.density).toInt()

        // View with elevation
        addView(
          FrameLayout(this@RoborazziViewWithElevationTestActivity)
            .apply {
              background = ColorDrawable(expectedViewBackgroundColor)
              elevation = 10f
              addView(TextView(this.context).apply { text = "Roborazzi" })
            }
            .also { view = it },
          LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.MATCH_PARENT
            )
            .apply { setMargins(10.toDp(), 10.toDp(), 10.toDp(), 10.toDp()) }
        )
      }
    )
  }
}

private class RoborazziDialogTestActivity : Activity() {
  var dialogContentView: View? = null
  val expectedDialogContentBackgroundColor: Int = Color.MAGENTA
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(
      LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        fun Int.toDp(): Int = (this * resources.displayMetrics.density).toInt()

        // View with elevation
        addView(
          TextView(this.context).apply { text = "Under the dialog" },
          LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.WRAP_CONTENT,
              LinearLayout.LayoutParams.WRAP_CONTENT
            )
            .apply { setMargins(10.toDp(), 10.toDp(), 10.toDp(), 10.toDp()) }
        )
      }
    )
    AlertDialog.Builder(this)
      .setTitle("Title")
      .setView(
        TextView(this).apply {
          dialogContentView = this
          text = "Roborazzi"
          setBackgroundColor(expectedDialogContentBackgroundColor)
        }
      )
      .setPositiveButton("OK") { _, _ -> }
      .show()
  }
}
