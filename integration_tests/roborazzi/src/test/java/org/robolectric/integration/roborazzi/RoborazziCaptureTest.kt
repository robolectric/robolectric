package org.robolectric.integration.roborazzi

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureScreenRoboImage
import kotlin.reflect.KClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.integration.roborazzi.RoborazziDialogTestActivity.Companion.OUTPUT_DIRECTORY_PATH

/**
 * Integration Test for Roborazzi
 *
 * This test is not intended to obstruct the release of Robolectric. In the event that issues are
 * detected which do not stem from Robolectric, the test can be temporarily disabled, and an issue
 * can be reported on the Roborazzi repository.
 *
 * Run ./gradlew integration_tests:roborazzi:recordRoborazziDebug
 * -Drobolectric.alwaysIncludeVariantMarkersInTestName=true to record the reference
 * screenshots(golden images). Run ./gradlew integration_tests:roborazzi:verifyRoborazziDebug
 * -Drobolectric.alwaysIncludeVariantMarkersInTestName=true to check the screenshots.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [S])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class RoborazziCaptureTest {
  @get:Rule
  val roborazziRule =
    RoborazziRule(
      options =
        RoborazziRule.Options(
          outputDirectoryPath = OUTPUT_DIRECTORY_PATH,
          roborazziOptions =
            RoborazziOptions(recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)),
        )
    )

  @Test
  // For reducing repository size, we use small size
  @Config(qualifiers = "w50dp-h40dp")
  fun checkViewWithElevationRendering() {
    hardwareRendererEnvironment {
      setupActivity(RoborazziViewWithElevationTestActivity::class)

      captureScreenWithRoborazzi()
    }
  }

  @Test
  // For reducing repository size, we use small size
  @Config(qualifiers = "w110dp-h120dp")
  fun checkDialogRendering() {
    hardwareRendererEnvironment {
      setupActivity(RoborazziDialogTestActivity::class)

      captureScreenWithRoborazzi()
    }
  }

  private fun setupActivity(activityClass: KClass<out Activity>) {
    registerActivityToPackageManager(checkNotNull(activityClass.java.canonicalName))
    ActivityScenario.launch(activityClass.java)
  }

  private fun captureScreenWithRoborazzi() {
    try {
      captureScreenRoboImage()
    } catch (e: AssertionError) {
      throw AssertionError(
        """
        |${e.message}
        |Please check the screenshot in $OUTPUT_DIRECTORY_PATH
        |If you want to update the screenshot, 
        |run `./gradlew integration_tests:roborazzi:recordRoborazziDebug -Drobolectric.alwaysIncludeVariantMarkersInTestName=true` and commit the changes.
        |"""
          .trimMargin(),
        e,
      )
    }
  }

  companion object {
    // TODO(hoisie): `robolectric.screenshot.hwrdr.native` is obsolete, remove it after the next
    // Robolectric point release.
    const val USE_HARDWARE_RENDERER_NATIVE_ENV = "robolectric.screenshot.hwrdr.native"
    const val PIXEL_COPY_RENDER_MODE = "robolectric.pixelCopyRenderMode"
  }
}

private fun registerActivityToPackageManager(activity: String) {
  val appContext: Application =
    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
  Shadows.shadowOf(appContext.packageManager)
    .addActivityIfNotPresent(ComponentName(appContext.packageName, activity))
}

@Suppress("ForbiddenComment")
private fun hardwareRendererEnvironment(block: () -> Unit) {
  val originalHwrdrOption =
    System.getProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV, null)
  val originalPixelCopyOption =
    System.getProperty(RoborazziCaptureTest.PIXEL_COPY_RENDER_MODE, null)
  // This cause ClassNotFoundException: java.nio.NioUtils
  // TODO: Remove comment out after fix this issue
  // https://github.com/robolectric/robolectric/issues/8081#issuecomment-1858726896
  // System.setProperty(USE_HARDWARE_RENDERER_NATIVE_ENV, "true")
  try {
    block()
  } finally {
    if (originalHwrdrOption == null) {
      System.clearProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV)
      System.clearProperty(RoborazziCaptureTest.PIXEL_COPY_RENDER_MODE)
    } else {
      System.setProperty(RoborazziCaptureTest.USE_HARDWARE_RENDERER_NATIVE_ENV, originalHwrdrOption)
      System.setProperty(RoborazziCaptureTest.PIXEL_COPY_RENDER_MODE, originalPixelCopyOption)
    }
  }
}

private class RoborazziViewWithElevationTestActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(android.R.style.Theme_Light_NoTitleBar)
    super.onCreate(savedInstanceState)
    setContentView(
      LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        fun Int.toDp(): Int = (this * resources.displayMetrics.density).toInt()

        // View with elevation
        addView(
          FrameLayout(this@RoborazziViewWithElevationTestActivity).apply {
            background = ColorDrawable(Color.MAGENTA)
            elevation = 10f
            addView(TextView(this.context).apply { text = "Txt" })
          },
          LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT,
              LinearLayout.LayoutParams.MATCH_PARENT,
            )
            .apply { setMargins(10.toDp(), 10.toDp(), 10.toDp(), 10.toDp()) },
        )
      }
    )
  }
}

private class RoborazziDialogTestActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(android.R.style.Theme_DeviceDefault_Light_NoActionBar)
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
              LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            .apply { setMargins(10.toDp(), 10.toDp(), 10.toDp(), 10.toDp()) },
        )
      }
    )
    AlertDialog.Builder(this).setTitle("Dlg").setPositiveButton("OK") { _, _ -> }.show()
  }

  companion object {
    const val OUTPUT_DIRECTORY_PATH = "src/screenshots"
  }
}
