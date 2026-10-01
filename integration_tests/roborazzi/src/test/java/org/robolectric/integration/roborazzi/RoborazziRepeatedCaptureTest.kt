package org.robolectric.integration.roborazzi

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36, 37], qualifiers = "w100dp-h100dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalRoborazziApi::class)
class RoborazziRepeatedCaptureTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Test fun captureScreen_afterContentChange_test1() = captureScreenAfterContentChange(Color.GREEN)

  @Test fun captureScreen_afterContentChange_test2() = captureScreenAfterContentChange(Color.BLUE)

  @Test fun captureScreen_afterContentChange_test3() = captureScreenAfterContentChange(Color.RED)

  @Test
  fun captureScreen_repeatedly_showsEachContentChange() {
    launchActivity().use { scenario ->
      scenario.onActivity { activity ->
        repeat(CAPTURES) { capture ->
          val color = COLORS[capture % COLORS.size]
          activity.setContentColor(color)

          assertWithMessage("capture %s", capture).that(captureScreen()).isEqualTo(color)
        }
      }
    }
  }

  @Test
  fun captureView_repeatedly_showsEachContentChange() {
    launchActivity().use { scenario ->
      scenario.onActivity { activity ->
        repeat(CAPTURES) { capture ->
          val color = COLORS[capture % COLORS.size]
          activity.setContentColor(color)

          val file = File(temporaryFolder.root, "view-$capture.png")
          activity.content.captureRoboImage(file, RECORD)
          assertWithMessage("capture %s", capture).that(centerPixel(file)).isEqualTo(color)
        }
      }
    }
  }

  private fun captureScreenAfterContentChange(color: Int) {
    launchActivity().use { scenario ->
      scenario.onActivity { activity ->
        activity.setContentColor(color)

        assertWithMessage("capture").that(captureScreen()).isEqualTo(color)
      }
    }
  }

  private fun captureScreen(): Int {
    val file = File(temporaryFolder.newFolder(), "screen.png")
    captureScreenRoboImage(file, RECORD)
    return centerPixel(file)
  }

  private fun centerPixel(file: File): Int {
    val bitmap = checkNotNull(BitmapFactory.decodeFile(file.path)) { "nothing recorded to $file" }
    return bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
  }

  private fun launchActivity(): ActivityScenario<RepeatedCaptureActivity> {
    val application = ApplicationProvider.getApplicationContext<Application>()
    shadowOf(application.packageManager)
      .addActivityIfNotPresent(
        ComponentName(application.packageName, RepeatedCaptureActivity::class.java.name)
      )
    return ActivityScenario.launch(RepeatedCaptureActivity::class.java)
  }

  private companion object {
    const val CAPTURES = 12
    val COLORS = intArrayOf(Color.GREEN, Color.BLUE, Color.RED, Color.MAGENTA)
    val RECORD = RoborazziOptions(taskType = RoborazziTaskType.Record)
  }
}

private class RepeatedCaptureActivity : Activity() {
  lateinit var content: View

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(android.R.style.Theme_Light_NoTitleBar)
    super.onCreate(savedInstanceState)
    content = View(this).apply { setBackgroundColor(Color.BLACK) }
    setContentView(content, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
  }

  fun setContentColor(color: Int) {
    content.setBackgroundColor(Color.BLACK)
    ShadowLooper.idleMainLooper()
    content.setBackgroundColor(color)
    ShadowLooper.idleMainLooper()
  }
}
