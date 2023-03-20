package org.robolectric.integrationtests.nativegraphics.testing.util;

import android.view.ViewTreeObserver.OnDrawListener;
import android.webkit.WebView;
import android.webkit.WebView.VisualStateCallback;
import android.webkit.WebViewClient;
import java.util.concurrent.CountDownLatch;

public final class WebViewReadyHelper {
  // Hacky quick-fix similar to DrawActivity's DrawCounterListener
  // TODO: De-dupe this against DrawCounterListener and fix this cruft
  private static final int DEBUG_REQUIRE_EXTRA_FRAMES = 1;
  private int drawCount = 0;

  private final CountDownLatch latch;
  private final WebView webView;

  public WebViewReadyHelper(WebView webView, CountDownLatch latch) {
    this.webView = webView;
    this.latch = latch;
    this.webView.setWebViewClient(client);
  }

  public void loadData(String data) {
    webView.loadData(data, null, null);
  }

  private WebViewClient client =
      new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
          webView.postVisualStateCallback(0, visualStateCallback);
        }
      };

  private VisualStateCallback visualStateCallback =
      new VisualStateCallback() {
        @Override
        public void onComplete(long requestId) {
          webView.getViewTreeObserver().addOnDrawListener(onDrawListener);
          webView.invalidate();
        }
      };

  private OnDrawListener onDrawListener =
      new OnDrawListener() {
        @Override
        public void onDraw() {
          if (++drawCount <= DEBUG_REQUIRE_EXTRA_FRAMES) {
            webView.postInvalidate();
            return;
          }

          webView.post(
              () -> {
                webView.getViewTreeObserver().removeOnDrawListener(onDrawListener);
                latch.countDown();
              });
        }
      };
}
