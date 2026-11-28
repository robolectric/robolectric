package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
public class ShadowSurfaceViewTest {

  // Windows are visible, as on a device; Robolectric's legacy graphics otherwise leave them GONE.
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setUp() {
    setSystemPropertyRule.set("robolectric.areWindowsMarkedVisible", "true");
  }

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

    assertThat(fakeSurfaceHolder.getCallbacks()).hasSize(2);

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

  @Test
  public void emitSurfaceChanged_invokesCallback() {
    TestCallback testCallback = new TestCallback();
    surfaceHolder.addCallback(testCallback);

    fakeSurfaceHolder.emitSurfaceChanged(/* format= */ 1, /* width= */ 2, /* height= */ 3);

    assertThat(testCallback.updatedFormat).isEqualTo(1);
    assertThat(testCallback.updatedWidth).isEqualTo(2);
    assertThat(testCallback.updatedHeight).isEqualTo(3);
  }

  @Test
  public void emitSurfaceCreatedAndDestroyed_invokeCallbacks() {
    TestCallback testCallback = new TestCallback();
    surfaceHolder.addCallback(testCallback);

    fakeSurfaceHolder.emitSurfaceCreated();
    assertThat(testCallback.created).isTrue();

    fakeSurfaceHolder.emitSurfaceDestroyed();
    assertThat(testCallback.destroyed).isTrue();
  }

  @Test
  public void lockCanvas_noSurfaceSize_returnsNull() {
    assertThat(surfaceHolder.getSurfaceFrame().isEmpty()).isTrue();
    assertThat(surfaceHolder.lockCanvas()).isNull();
  }

  @Test
  @Config(minSdk = O)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void lockCanvas_afterSurfaceChanged_drawsPostedFrame() {
    fakeSurfaceHolder.emitSurfaceChanged(PixelFormat.RGBA_8888, 10, 20);
    assertThat(surfaceHolder.getSurfaceFrame()).isEqualTo(new Rect(0, 0, 10, 20));
    assertThat(fakeSurfaceHolder.getLastPostedFrame()).isNull();

    Canvas canvas = surfaceHolder.lockCanvas();
    canvas.drawColor(Color.RED);
    surfaceHolder.unlockCanvasAndPost(canvas);

    Bitmap frame = fakeSurfaceHolder.getLastPostedFrame();
    assertThat(frame.getWidth()).isEqualTo(10);
    assertThat(frame.getHeight()).isEqualTo(20);
    assertThat(frame.getPixel(5, 10)).isEqualTo(Color.RED);
  }

  @Test
  @Config(minSdk = O)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void lockCanvas_dirtyRect_keepsRestOfLastFrame() {
    fakeSurfaceHolder.emitSurfaceChanged(PixelFormat.RGBA_8888, 10, 10);
    Canvas canvas = surfaceHolder.lockCanvas();
    canvas.drawColor(Color.RED);
    surfaceHolder.unlockCanvasAndPost(canvas);

    canvas = surfaceHolder.lockCanvas(new Rect(0, 0, 5, 10));
    canvas.drawColor(Color.BLUE);
    surfaceHolder.unlockCanvasAndPost(canvas);

    Bitmap frame = fakeSurfaceHolder.getLastPostedFrame();
    assertThat(frame.getPixel(2, 5)).isEqualTo(Color.BLUE);
    assertThat(frame.getPixel(7, 5)).isEqualTo(Color.RED);
  }

  @Test
  public void getSurfaceFrame_fixedSize_overridesOtherSizes() {
    fakeSurfaceHolder.emitSurfaceChanged(PixelFormat.RGBA_8888, 10, 10);

    surfaceHolder.setFixedSize(30, 40);

    assertThat(surfaceHolder.getSurfaceFrame()).isEqualTo(new Rect(0, 0, 30, 40));
    surfaceHolder.setSizeFromLayout();
    assertThat(surfaceHolder.getSurfaceFrame()).isEqualTo(new Rect(0, 0, 10, 10));
  }

  @Test
  public void getSurfaceFrame_laidOutView_isViewSize() {
    view.layout(0, 0, 60, 70);

    assertThat(surfaceHolder.getSurfaceFrame()).isEqualTo(new Rect(0, 0, 60, 70));
  }

  @Test
  public void attached_byDefault_runsOnlyEmittedCallbacks() {
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);
      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      shadowOf(surfaceView).getFakeSurfaceHolder().emitSurfaceChanged(PixelFormat.RGB_565, 10, 20);

      assertThat(callback.events).containsExactly("changed 10x20");
      assertThat(surfaceView.getHolder().getSurface()).isNull();
    }
  }

  @Test
  public void glSurfaceView_followsViewLifecycle_isNotCreated() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      GLSurfaceView glSurfaceView = new GLSurfaceView(controller.get());
      glSurfaceView.setRenderer(
          new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {}

            @Override
            public void onDrawFrame(GL10 gl) {}
          });
      LifecycleCallback callback = new LifecycleCallback();
      glSurfaceView.getHolder().addCallback(callback);

      controller.get().setContentView(glSurfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      assertThat(callback.events).isEmpty();
      assertThat(glSurfaceView.getHolder().getSurface()).isNull();
    }
  }

  @Test
  public void attachedAndLaidOut_createsSurface() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);

      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      assertThat(callback.events).containsExactly("created(creating)", "changed 100x50").inOrder();
      assertThat(surfaceView.getHolder().getSurface()).isNotNull();
      assertThat(surfaceView.getHolder().getSurfaceFrame()).isEqualTo(new Rect(0, 0, 100, 50));
      assertThat(surfaceView.getHolder().isCreating()).isFalse();
    }
  }

  @Test
  public void attachedAndLaidOut_surfaceIsValid() {
    // Robolectric's native graphics cannot make a valid Surface before P.
    assume().that(RuntimeEnvironment.getApiLevel() >= P || !ShadowView.useRealGraphics()).isTrue();
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      assertThat(surfaceView.getHolder().getSurface().isValid()).isTrue();
    }
  }

  @Test
  public void resized_reportsChangedSize() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);
      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();
      Surface surface = surfaceView.getHolder().getSurface();

      surfaceView.setLayoutParams(new FrameLayout.LayoutParams(80, 60));
      ShadowLooper.idleMainLooper();

      assertThat(callback.events)
          .containsExactly("created(creating)", "changed 100x50", "changed 80x60")
          .inOrder();
      assertThat(surfaceView.getHolder().getSurface()).isSameInstanceAs(surface);
    }
  }

  @Test
  public void hiddenAndShown_destroysAndRecreatesSurface() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);
      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      surfaceView.setVisibility(View.GONE);
      ShadowLooper.idleMainLooper();
      assertThat(surfaceView.getHolder().getSurface()).isNull();

      surfaceView.setVisibility(View.VISIBLE);
      ShadowLooper.idleMainLooper();

      assertThat(callback.events)
          .containsExactly(
              "created(creating)",
              "changed 100x50",
              "destroyed",
              "created(creating)",
              "changed 100x50")
          .inOrder();
    }
  }

  @Test
  public void detached_destroysSurface() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);
      FrameLayout content = new FrameLayout(controller.get());
      content.addView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      controller.get().setContentView(content);
      ShadowLooper.idleMainLooper();
      Surface surface = surfaceView.getHolder().getSurface();

      content.removeView(surfaceView);
      ShadowLooper.idleMainLooper();

      assertThat(callback.events)
          .containsExactly("created(creating)", "changed 100x50", "destroyed")
          .inOrder();
      assertThat(surface.isValid()).isFalse();
    }
  }

  @Test
  public void setFixedSize_whileCreated_reportsFixedSize() {
    ShadowSurfaceView.setFollowsViewLifecycle(true);
    try (ActivityController<Activity> controller = buildActivity(Activity.class).setup()) {
      SurfaceView surfaceView = new SurfaceView(controller.get());
      LifecycleCallback callback = new LifecycleCallback();
      surfaceView.getHolder().addCallback(callback);
      controller.get().setContentView(surfaceView, new FrameLayout.LayoutParams(100, 50));
      ShadowLooper.idleMainLooper();

      surfaceView.getHolder().setFixedSize(20, 10);

      assertThat(callback.events)
          .containsExactly("created(creating)", "changed 100x50", "changed 20x10")
          .inOrder();
    }
  }

  /** Records each callback, and whether the holder said it was creating during surfaceCreated. */
  private static class LifecycleCallback implements SurfaceHolder.Callback {
    final List<String> events = new ArrayList<>();

    @Override
    public void surfaceCreated(@Nonnull SurfaceHolder holder) {
      events.add(holder.isCreating() ? "created(creating)" : "created");
    }

    @Override
    public void surfaceChanged(@Nonnull SurfaceHolder holder, int format, int w, int h) {
      events.add("changed " + w + "x" + h);
    }

    @Override
    public void surfaceDestroyed(@Nonnull SurfaceHolder holder) {
      events.add("destroyed");
    }
  }

  private static class TestCallback implements SurfaceHolder.Callback {
    int updatedFormat;
    int updatedWidth;
    int updatedHeight;
    boolean created;
    boolean destroyed;

    @Override
    public void surfaceCreated(@Nonnull SurfaceHolder holder) {
      created = true;
    }

    @Override
    public void surfaceDestroyed(@Nonnull SurfaceHolder holder) {
      destroyed = true;
    }

    @Override
    public void surfaceChanged(@Nonnull SurfaceHolder holder, int format, int w, int h) {
      updatedFormat = format;
      updatedWidth = w;
      updatedHeight = h;
    }
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
