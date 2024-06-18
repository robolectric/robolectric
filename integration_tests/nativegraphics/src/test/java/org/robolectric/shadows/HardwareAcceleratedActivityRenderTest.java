package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Q)
public class HardwareAcceleratedActivityRenderTest {
  @Test
  public void hardwareAcceleratedActivity_setup() throws Exception {
    // Setting up an Activity is a smoke test that exercises much of the HardwareRenderer /
    // RenderNode / RecordingCanvas native code.
    Robolectric.setupActivity(HardwareAcceleratedActivity.class);
  }

  @Test
  public void hardwareAcceleratedActivity_pixelCopy() throws Exception {
    // This API is not supported correctly on macOS now.
    assume()
        .that(
            Objects.requireNonNull(System.getProperty("os.name"))
                .toLowerCase(Locale.US)
                .contains("mac"))
        .isFalse();
    System.setProperty("robolectric.pixelCopyRenderMode", "hardware");
    try {
      HardwareAcceleratedActivity activity =
          Robolectric.setupActivity(HardwareAcceleratedActivity.class);
      Window window = activity.getWindow();
      View decorView = window.getDecorView();
      Bitmap bitmap =
          Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
      CountDownLatch latch = new CountDownLatch(1);
      PixelCopy.request(
          window, bitmap, copyResult -> latch.countDown(), new Handler(Looper.getMainLooper()));
      latch.await(1, TimeUnit.SECONDS);
      assertThat(bitmap.getPixel(100, 100)).isEqualTo(Color.RED);
    } finally {
      System.clearProperty("robolectric.pixelCopyRenderMode");
    }
  }

  static class HardwareAcceleratedActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      FrameLayout frameLayout = new FrameLayout(this);
      frameLayout.setLayoutParams(
          new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      frameLayout.setBackgroundColor(Color.RED);
      setContentView(frameLayout);
    }
  }
}
