package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

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
import java.time.Duration;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

/**
 * Verifies that on SDK 37 (real draw traversals unconditionally enabled), PixelCopy captures draws
 * performed after the initial traversal, in every test of a JVM process.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 37)
@GraphicsMode(Mode.NATIVE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ShadowNativeHardwareRendererFrameTimestampTest {

  @Test
  public void pixelCopy_afterContentChange_test1() throws Exception {
    ShadowPausedSystemClock.advanceBy(Duration.ofMillis(5000));
    runContentChangeScenario();
  }

  @Test
  public void pixelCopy_afterContentChange_test2() throws Exception {
    runContentChangeScenario();
  }

  private static void runContentChangeScenario() throws Exception {
    RedActivity activity = Robolectric.setupActivity(RedActivity.class);

    // Sanity check: initial traversal should have produced a RED frame.
    assertThat(captureWindowPixel(activity)).isEqualTo(Color.RED);

    // Change content after the initial traversal, then let the scheduled traversal run.
    activity.frameLayout.setBackgroundColor(Color.BLUE);
    ShadowLooper.idleMainLooper();

    assertThat(captureWindowPixel(activity)).isEqualTo(Color.BLUE);
  }

  private static int captureWindowPixel(Activity activity) throws Exception {
    Window window = activity.getWindow();
    View decorView = window.getDecorView();
    Bitmap bitmap =
        Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
    int[] result = new int[] {-1};
    PixelCopy.request(
        window,
        bitmap,
        copyResult -> {
          result[0] = copyResult;
        },
        new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();
    assertThat(result[0]).isEqualTo(PixelCopy.SUCCESS);
    return bitmap.getPixel(100, 100);
  }

  static class RedActivity extends Activity {
    FrameLayout frameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      frameLayout = new FrameLayout(this);
      frameLayout.setLayoutParams(
          new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      frameLayout.setBackgroundColor(Color.RED);
      setContentView(frameLayout);
    }
  }
}
