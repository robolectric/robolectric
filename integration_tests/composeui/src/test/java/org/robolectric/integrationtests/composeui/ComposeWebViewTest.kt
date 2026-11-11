package org.robolectric.integrationtests.composeui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowCanvas

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.LEGACY)
class ComposeWebViewTest {
  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun androidViewHostsWebView() {
    val html = "<html><title>Compose WebView</title></html>"
    var webView: WebView? = null

    composeRule.setContent {
      AndroidView(
        factory = { context ->
          WebView(context).also {
            webView = it
            it.loadData(html, "text/html", "UTF-8")
          }
        },
      )
    }
    composeRule.waitForIdle()

    val hostedWebView = checkNotNull(webView)
    assertThat(hostedWebView.isAttachedToWindow).isTrue()
    assertThat(shadowOf(hostedWebView).lastLoadData.data).isEqualTo(html)
  }

  @Test
  fun androidViewDrawsWebViewBackground() {
    var webView: WebView? = null
    composeRule.setContent {
      AndroidView(
        modifier = Modifier.size(100.dp),
        factory = { context ->
          WebView(context).also {
            webView = it
            it.background = ColorDrawable(Color.RED)
          }
        },
      )
    }
    composeRule.waitForIdle()

    val canvas = Canvas()
    checkNotNull(webView).draw(canvas)

    assertThat(ShadowCanvas.visualize(canvas)).contains("background:")
  }

  @Test
  fun composeWebViewSimulatesEspressoWebJavaScriptCallback() {
    val html = "<html><body>Compose WebView</body></html>"
    var webView: WebView? = null
    composeRule.setContent {
      AndroidView(
        modifier = Modifier.size(100.dp),
        factory = { context ->
          WebView(context).also {
            webView = it
            it.settings.javaScriptEnabled = true
            it.loadData(html, "text/html", "UTF-8")
          }
        },
      )
    }
    composeRule.waitForIdle()

    var result: String? = null
    val script = "document.body.innerText"
    val hostedWebView = checkNotNull(webView)
    hostedWebView.evaluateJavascript(script) { result = it }

    val shadowWebView = shadowOf(hostedWebView)
    assertThat(shadowWebView.lastEvaluatedJavascript).isEqualTo(script)
    shadowWebView.lastEvaluatedJavascriptCallback?.onReceiveValue("\"Compose WebView\"")
    assertThat(result).isEqualTo("\"Compose WebView\"")
  }
}
