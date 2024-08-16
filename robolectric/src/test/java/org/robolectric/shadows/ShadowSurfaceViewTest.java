package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;

@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceViewTest {

  private final SurfaceHolder.Callback callback1 = new TestCallback();
  private final SurfaceHolder.Callback callback2 = new TestCallback();
  private final ActivityController<Activity> defaultController = buildActivity(Activity.class);

  private final SurfaceView view = new SurfaceView(defaultController.create().get());
  private final SurfaceHolder surfaceHolder = view.getHolder();
  private final ShadowSurfaceView shadowSurfaceView = Shadows.shadowOf(view);
  private final ShadowSurfaceView.FakeSurfaceHolder fakeSurfaceHolder =
      shadowSurfaceView.getFakeSurfaceHolder();

  @After
  public void tearDown() {
    defaultController.close();
  }

  @Test
  public void addCallback() {
    assertThat(fakeSurfaceHolder.getCallbacks()).isEmpty();

    surfaceHolder.addCallback(callback1);

    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback1);

    surfaceHolder.addCallback(callback2);

    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback1);
    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback2);
  }

  @Test
  public void removeCallback() {
    surfaceHolder.addCallback(callback1);
    surfaceHolder.addCallback(callback2);

    assertThat(fakeSurfaceHolder.getCallbacks().size()).isEqualTo(2);

    surfaceHolder.removeCallback(callback1);

    assertThat(fakeSurfaceHolder.getCallbacks()).doesNotContain(callback1);
    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback2);
  }

  @Test
  public void canCreateASurfaceView_attachedToAWindowWithActionBar() {
    try (ActivityController<TestActivity> controller = buildActivity(TestActivity.class)) {
      TestActivity testActivity = controller.create().start().resume().visible().get();
      assertThat(testActivity).isNotNull();
    }
  }

  @Test
  public void requestedFormat_default_getRGB565() {
    assertThat(fakeSurfaceHolder.getRequestedFormat()).isEqualTo(PixelFormat.RGB_565);
  }

  @Test
  public void requestedFormat_setNewFormat_getNewFormat() {
    view.getHolder().setFormat(PixelFormat.HSV_888);
    assertThat(fakeSurfaceHolder.getRequestedFormat()).isEqualTo(PixelFormat.HSV_888);
  }

  @Test
  public void requestedFormat_setFormatOpaque_getRGB565() {
    view.getHolder().setFormat(PixelFormat.OPAQUE);
    assertThat(fakeSurfaceHolder.getRequestedFormat()).isEqualTo(PixelFormat.RGB_565);
  }

  private static class TestCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {}
  }

  private static class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_ACTION_BAR);
      setContentView(new SurfaceView(this));
    }
  }
}
