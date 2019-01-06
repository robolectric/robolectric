package org.robolectric.kotlin

import android.app.Application
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.R
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.kotlin.TestApis.testApi
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
class TestApisTest {

  private lateinit var context: Application

  @Before
  @Throws(Exception::class)
  fun setUp() {
    context = RuntimeEnvironment.application
  }


  @Test
  fun shouldThingy() {
    val application = RuntimeEnvironment.application
  }

  @Test
  @Throws(Exception::class)
  fun shouldHaveShortDuration() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    assertThat(toast).isNotNull()
    assertThat(toast.duration).isEqualTo(Toast.LENGTH_SHORT)
  }

  @Test
  @Throws(Exception::class)
  fun shouldHaveLongDuration() {
    val toast = Toast.makeText(context, "long toast", Toast.LENGTH_LONG)
    assertThat(toast).isNotNull()
    assertThat(toast.duration).isEqualTo(Toast.LENGTH_LONG)
  }

  @Test
  @Throws(Exception::class)
  fun shouldMakeTextCorrectly() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    assertThat(toast).isNotNull()
    assertThat(toast.duration).isEqualTo(Toast.LENGTH_SHORT)
    toast.show()
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast)
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("short toast")
    assertThat(ShadowToast.showedToast("short toast")).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetTextCorrectly() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    toast.setText("other toast")
    toast.show()
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast)
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("other toast")
    assertThat(ShadowToast.showedToast("other toast")).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetTextWithIdCorrectly() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    toast.setText(R.string.hello)
    toast.show()
    assertThat(ShadowToast.getLatestToast()).isSameAs(toast)
    assertThat(ShadowToast.getTextOfLatestToast()).isEqualTo("Hello")
    assertThat(ShadowToast.showedToast("Hello")).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetViewCorrectly() {
    val toast = Toast(context)
    toast.duration = Toast.LENGTH_SHORT
    val view = TextView(context)
    toast.view = view
    assertThat(toast.view).isSameAs(view)
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetGravityCorrectly() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    assertThat(toast).isNotNull()
    toast.setGravity(Gravity.CENTER, 0, 0)
    assertThat(toast.gravity).isEqualTo(Gravity.CENTER)
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetOffsetsCorrectly() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    toast.setGravity(0, 12, 34)
    assertThat(toast.xOffset).isEqualTo(12)
    assertThat(toast.yOffset).isEqualTo(34)
  }

  @Test
  @Throws(Exception::class)
  fun shouldCountToastsCorrectly() {
    assertThat(ShadowToast.shownToastCount()).isEqualTo(0)
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    assertThat(toast).isNotNull()
    toast.show()
    toast.show()
    toast.show()
    assertThat(ShadowToast.shownToastCount()).isEqualTo(3)
    ShadowToast.reset()
    assertThat(ShadowToast.shownToastCount()).isEqualTo(0)
    toast.show()
    toast.show()
    assertThat(ShadowToast.shownToastCount()).isEqualTo(2)
  }

  @Test
  @Throws(Exception::class)
  fun shouldBeCancelled() {
    val toast = Toast.makeText(context, "short toast", Toast.LENGTH_SHORT)
    toast.cancel()
    assertThat(toast.testApi.isCancelled).isTrue()
  }
}
