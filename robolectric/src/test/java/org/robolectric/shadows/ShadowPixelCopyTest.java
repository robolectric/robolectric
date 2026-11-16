package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;

/** Tests for {@link ShadowPixelCopy} when real draw traversals are enabled. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = TIRAMISU)
@GraphicsMode(Mode.NATIVE)
public class ShadowPixelCopyTest {

  @Before
  public void setUp() {
    ShadowView.setUseRealDrawTraversals(true);
  }

  @After
  public void tearDown() {
    ShadowView.setUseRealDrawTraversals(false);
  }

  @Test
  public void requestSurfaceView_realDrawTraversals_drawsSurfaceView() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    SurfaceView surfaceView = new SurfaceView(activity);
    surfaceView.setBackgroundColor(Color.RED);
    activity.setContentView(surfaceView, new FrameLayout.LayoutParams(100, 100));
    ShadowLooper.idleMainLooper();
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    AtomicInteger result = new AtomicInteger(-1);

    PixelCopy.request(surfaceView, bitmap, result::set, new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    assertThat(result.get()).isEqualTo(PixelCopy.SUCCESS);
    assertThat(bitmap.getPixel(50, 50)).isEqualTo(Color.RED);
  }

  @Test
  public void requestSurfaceViewWithSrcRect_realDrawTraversals_drawsSurfaceView() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();
    SurfaceView surfaceView = new SurfaceView(activity);
    surfaceView.setBackgroundColor(Color.RED);
    surfaceView.layout(0, 0, 100, 100);
    Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
    AtomicInteger result = new AtomicInteger(-1);

    PixelCopy.request(
        surfaceView,
        new Rect(10, 10, 60, 60),
        bitmap,
        result::set,
        new Handler(Looper.getMainLooper()));
    ShadowLooper.idleMainLooper();

    assertThat(result.get()).isEqualTo(PixelCopy.SUCCESS);
    assertThat(bitmap.getPixel(25, 25)).isEqualTo(Color.RED);
  }
}
