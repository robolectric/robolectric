package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplayConfig;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit tests for {@link ShadowDisplayManagerGlobal} */
@RunWith(AndroidJUnit4.class)
public class ShadowDisplayManagerGlobalTest {

  private static final int DISPLAY_WIDTH = 720;
  private static final int DISPLAY_HEIGHT = 1280;
  private static final int DISPLAY_DPI = 160;

  @LazyApplication(LazyLoad.ON)
  @Test
  public void testDisplayManagerGlobalIsLazyLoaded() {
    assertThat(ShadowDisplayManagerGlobal.getGlobalInstance()).isNull();
    assertThat(DisplayManagerGlobal.getInstance().getRealDisplay(Display.DEFAULT_DISPLAY))
        .isNotNull();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void testVirtualDisplay_create() {
    Surface surface = new Surface(new SurfaceTexture(0));
    VirtualDisplay virtualDisplay = createVirtualDisplay(surface);

    Rect size = new Rect();
    virtualDisplay.getDisplay().getRectSize(size);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    virtualDisplay.getDisplay().getMetrics(displayMetrics);

    assertThat(displayMetrics.densityDpi).isEqualTo(DISPLAY_DPI);
    assertThat(displayMetrics.heightPixels).isEqualTo(DISPLAY_HEIGHT);
    assertThat(displayMetrics.widthPixels).isEqualTo(DISPLAY_WIDTH);
    assertThat(size).isEqualTo(new Rect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT));

    assertThat(virtualDisplay.getSurface()).isEqualTo(surface);
    assertThat(virtualDisplay.getDisplay().getDisplayId()).isNotEqualTo(Display.DEFAULT_DISPLAY);
    assertThat(virtualDisplay.getDisplay().getName()).isEqualTo("name");
    assertThat(virtualDisplay.getDisplay().getFlags()).isEqualTo(123);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void testVirtualDisplay_resize() {
    VirtualDisplay virtualDisplay = createVirtualDisplay(null);

    virtualDisplay.resize(DISPLAY_WIDTH + 1, DISPLAY_HEIGHT + 1, DISPLAY_DPI + 1);

    Rect size = new Rect();
    virtualDisplay.getDisplay().getRectSize(size);
    DisplayMetrics displayMetrics = new DisplayMetrics();
    virtualDisplay.getDisplay().getMetrics(displayMetrics);

    assertThat(displayMetrics.densityDpi).isEqualTo(DISPLAY_DPI + 1);
    assertThat(displayMetrics.heightPixels).isEqualTo(DISPLAY_HEIGHT + 1);
    assertThat(displayMetrics.widthPixels).isEqualTo(DISPLAY_WIDTH + 1);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void testVirtualDisplay_release() {
    VirtualDisplay virtualDisplay = createVirtualDisplay(null);
    int displayId = virtualDisplay.getDisplay().getDisplayId();
    virtualDisplay.release();

    assertThat(DisplayManagerGlobal.getInstance().getDisplayInfo(displayId)).isNull();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void testVirtualDisplay_setSurfaceTogglesState() {
    Surface surface = new Surface(new SurfaceTexture(0));
    VirtualDisplay virtualDisplay = createVirtualDisplay(surface);

    assertThat(virtualDisplay.getDisplay().getState()).isEqualTo(Display.STATE_ON);
    DisplayListener listener = mock(DisplayListener.class);
    getApplicationContext()
        .getSystemService(DisplayManager.class)
        .registerDisplayListener(listener, new Handler(Looper.getMainLooper()));

    // Set the surface to null and verify display turns off and listeners are notified.
    virtualDisplay.setSurface(null);
    ShadowLooper.idleMainLooper();

    assertThat(virtualDisplay.getDisplay().getState()).isEqualTo(Display.STATE_OFF);
    verify(listener).onDisplayChanged(virtualDisplay.getDisplay().getDisplayId());
  }

  private VirtualDisplay createVirtualDisplay(Surface surface) {
    VirtualDisplayConfig config =
        new VirtualDisplayConfig.Builder("name", DISPLAY_WIDTH, DISPLAY_HEIGHT, DISPLAY_DPI)
            .setSurface(surface)
            .setFlags(123)
            .build();
    if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
      return ReflectionHelpers.callInstanceMethod(
          DisplayManagerGlobal.getInstance(),
          "createVirtualDisplay",
          ClassParameter.from(Context.class, getApplicationContext()),
          ClassParameter.from(MediaProjection.class, null),
          ClassParameter.from(VirtualDisplayConfig.class, config),
          ClassParameter.from(VirtualDisplay.Callback.class, mock(VirtualDisplay.Callback.class)),
          ClassParameter.from(Executor.class, MoreExecutors.directExecutor()),
          ClassParameter.from(Context.class, getApplicationContext()));
    } else {
      return DisplayManagerGlobal.getInstance()
          .createVirtualDisplay(
              getApplicationContext(),
              null,
              config,
              mock(VirtualDisplay.Callback.class),
              MoreExecutors.directExecutor());
    }
  }
}
