package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNull;
import static org.robolectric.annotation.GraphicsMode.Mode.NATIVE;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
@GraphicsMode(NATIVE)
public final class ShadowTextureViewTest {
  private final List<String> events = new ArrayList<>();

  @Test
  @Config(minSdk = Q)
  public void testTextureView_draw_doesNotCrash() {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);
    TextureView textureView = activity.textureView;

    RenderNode renderNode = new RenderNode("MyRenderNode");
    RecordingCanvas canvas = renderNode.beginRecording();
    assertThat(canvas.isHardwareAccelerated()).isTrue();
    assertNull(ReflectionHelpers.callInstanceMethod(textureView, "getTextureLayer"));

    try {
      textureView.draw(canvas);
    } finally {
      renderNode.endRecording();
    }
  }

  @Test
  public void byDefault_noSurfaceTexture() {
    FrameLayout content = setUpContent();

    TextureView view = add(content, 200, 100);

    assertThat(view.isAvailable()).isFalse();
    assertThat(view.getSurfaceTexture()).isNull();
    assertThat(events).isEmpty();
  }

  @Test
  public void providesSurfaceTexture_layoutMakesTexture() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();

    TextureView view = add(content, 200, 100);

    assertThat(view.isAvailable()).isTrue();
    assertThat(events).containsExactly("available 200x100");
    assertThat(new Surface(view.getSurfaceTexture()).isValid()).isTrue();
  }

  @Test
  public void providesSurfaceTexture_collapsedView_noTexture() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();

    TextureView view = add(content, 200, 0);

    assertThat(view.isAvailable()).isFalse();
    assertThat(events).isEmpty();
  }

  @Test
  public void providesSurfaceTexture_lockCanvas_postsFrameTheViewDraws() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    Surface surface = new Surface(view.getSurfaceTexture());

    post(surface, Color.RED);

    assertThat(events).contains("updated");
    assertThat(view.getBitmap().getPixel(10, 10)).isEqualTo(Color.RED);
    assertThat(drawContent(content).getPixel(100, 50)).isEqualTo(Color.RED);
  }

  @Test
  public void providesSurfaceTexture_lockHardwareCanvas_postsFrame() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    Surface surface = new Surface(view.getSurfaceTexture());

    Canvas canvas = surface.lockHardwareCanvas();
    canvas.drawColor(Color.BLUE);
    surface.unlockCanvasAndPost(canvas);
    shadowMainLooper().idle();

    assertThat(view.getBitmap().getPixel(100, 50)).isEqualTo(Color.BLUE);
  }

  @Test
  public void providesSurfaceTexture_transformPlacesFrame() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    // Translucent, so the content shows where the scaled frame does not reach.
    view.setOpaque(false);
    post(new Surface(view.getSurfaceTexture()), Color.GREEN);
    Matrix half = new Matrix();
    half.setScale(0.5f, 1f);

    view.setTransform(half);
    shadowMainLooper().idle();

    Bitmap drawn = drawContent(content);
    assertThat(drawn.getPixel(50, 50)).isEqualTo(Color.GREEN);
    assertThat(drawn.getPixel(150, 50)).isEqualTo(Color.WHITE);
  }

  @Test
  public void providesSurfaceTexture_resize_tellsListener() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    events.clear();

    view.setLayoutParams(new FrameLayout.LayoutParams(120, 80));
    shadowMainLooper().idle();

    assertThat(events).containsExactly("changed 120x80");
    Surface surface = new Surface(view.getSurfaceTexture());
    Canvas canvas = surface.lockCanvas(null);
    assertThat(canvas.getWidth()).isEqualTo(120);
    assertThat(canvas.getHeight()).isEqualTo(80);
    surface.unlockCanvasAndPost(canvas);
  }

  @Test
  public void providesSurfaceTexture_detach_destroysTexture() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    Surface surface = new Surface(view.getSurfaceTexture());
    events.clear();

    content.removeView(view);
    shadowMainLooper().idle();

    assertThat(events).containsExactly("destroyed");
    assertThat(view.getSurfaceTexture()).isNull();
    assertThat(surface.isValid()).isFalse();
  }

  @Test
  public void providesSurfaceTexture_appTexture_framesReachItsListener() {
    ShadowTextureView.setProvidesSurfaceTexture(true);
    FrameLayout content = setUpContent();
    TextureView view = add(content, 200, 100);
    SurfaceTexture texture = new SurfaceTexture(false);
    AtomicInteger framesAvailable = new AtomicInteger();
    texture.setOnFrameAvailableListener(t -> framesAvailable.incrementAndGet());

    view.setSurfaceTexture(texture);
    drawContent(content);
    post(new Surface(texture), Color.MAGENTA);

    assertThat(framesAvailable.get()).isEqualTo(1);
    assertThat(view.getBitmap().getPixel(100, 50)).isEqualTo(Color.MAGENTA);
  }

  @Test
  public void providesSurfaceTexture_otherSurfaces_unchanged() {
    boolean validBefore = new Surface(new SurfaceTexture(false)).isValid();

    ShadowTextureView.setProvidesSurfaceTexture(true);

    assertThat(new Surface(new SurfaceTexture(false)).isValid()).isEqualTo(validBefore);
  }

  private FrameLayout setUpContent() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    FrameLayout content = new FrameLayout(activity);
    content.setBackgroundColor(Color.WHITE);
    activity.setContentView(content);
    shadowMainLooper().idle();
    return content;
  }

  private TextureView add(FrameLayout content, int width, int height) {
    TextureView view = new TextureView(content.getContext());
    view.setSurfaceTextureListener(new Recorder());
    content.addView(view, new FrameLayout.LayoutParams(width, height));
    shadowMainLooper().idle();
    return view;
  }

  private static void post(Surface surface, int color) {
    Canvas canvas = surface.lockCanvas(null);
    canvas.drawColor(color);
    surface.unlockCanvasAndPost(canvas);
    shadowMainLooper().idle();
  }

  /** The content as its views draw it, as a software screenshot does. */
  private static Bitmap drawContent(View content) {
    Bitmap bitmap =
        Bitmap.createBitmap(content.getWidth(), content.getHeight(), Bitmap.Config.ARGB_8888);
    content.draw(new Canvas(bitmap));
    return bitmap;
  }

  private final class Recorder implements TextureView.SurfaceTextureListener {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      events.add("available " + width + "x" + height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
      events.add("changed " + width + "x" + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      events.add("destroyed");
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
      events.add("updated");
    }
  }

  private static class TestActivity extends Activity {
    public TextureView textureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      FrameLayout frameLayout = new FrameLayout(this);
      textureView = new TextureView(this);
      frameLayout.addView(textureView);
      setContentView(frameLayout);
    }
  }
}
