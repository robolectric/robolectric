package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.SurfaceView;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.util.ReflectionHelpers;

/** Checks that PixelCopy releases its owned intermediate while preserving the caller's bitmap. */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 36)
@GraphicsMode(Mode.NATIVE)
public class ShadowPixelCopyBitmapLifetimeTest {
  @Rule public SetSystemPropertyRule properties = new SetSystemPropertyRule();

  @Before
  public void selectLegacyCapturePath() {
    // This suite verifies the legacy copy path; SDK 37 always uses real draw traversals.
    properties.set("robolectric.useRealDrawTraversals", "false");
    ShadowView.setUseRealDrawTraversals(false);
    assertThat(ShadowView.areRealDrawTraversalsEnabled()).isFalse();
  }

  @Test
  public void softwareCopy_recyclesIntermediateAfterCopy() {
    properties.set("robolectric.pixelCopyRenderMode", "software");
    RecordingSurfaceView source = new RecordingSurfaceView();
    Bitmap destination = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
    AtomicInteger result = new AtomicInteger(-1);

    PixelCopy.request(source, destination, result::set, new Handler(Looper.getMainLooper()));

    assertThat(result.get()).isEqualTo(PixelCopy.SUCCESS);
    assertThat(source.intermediate.isRecycled()).isTrue();
    assertDetached(source.canvas);
    assertThat(destination.isRecycled()).isFalse();
    assertThat(destination.getPixel(0, 0)).isEqualTo(Color.RED);
    assertThat(destination.getPixel(3, 3)).isEqualTo(Color.BLUE);
    destination.eraseColor(Color.GREEN);
    assertThat(destination.getPixel(0, 0)).isEqualTo(Color.GREEN);
  }

  @Test
  public void softwareCopy_cropAndScaleRemainReadable() {
    properties.set("robolectric.pixelCopyRenderMode", "software");
    RecordingSurfaceView source = new RecordingSurfaceView();
    Bitmap destination = Bitmap.createBitmap(8, 6, Bitmap.Config.ARGB_8888);

    PixelCopy.request(
        source,
        new Rect(3, 0, 4, 4),
        destination,
        result -> assertThat(result).isEqualTo(PixelCopy.SUCCESS),
        new Handler(Looper.getMainLooper()));

    assertThat(source.intermediate.isRecycled()).isTrue();
    assertDetached(source.canvas);
    assertThat(destination.isRecycled()).isFalse();
    for (int y = 0; y < destination.getHeight(); y++) {
      for (int x = 0; x < destination.getWidth(); x++) {
        assertThat(destination.getPixel(x, y)).isEqualTo(Color.BLUE);
      }
    }
  }

  @Test
  public void softwareCopy_listenerCanReadCopiedPixelsAfterCleanup() {
    properties.set("robolectric.pixelCopyRenderMode", "software");
    RecordingSurfaceView source = new RecordingSurfaceView();
    Bitmap destination = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);

    PixelCopy.request(
        source,
        destination,
        result -> {
          assertThat(result).isEqualTo(PixelCopy.SUCCESS);
          assertThat(source.intermediate.isRecycled()).isTrue();
          assertDetached(source.canvas);
          assertThat(destination.getPixel(3, 3)).isEqualTo(Color.BLUE);
        },
        new Handler(Looper.getMainLooper()));
  }

  @Test
  public void softwareCopy_renderFailureRecyclesIntermediate() {
    properties.set("robolectric.pixelCopyRenderMode", "software");
    RecordingSurfaceView source = new RecordingSurfaceView();
    RuntimeException failure = new RuntimeException("render failed");
    source.drawFailure = failure;
    Bitmap destination = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);
    destination.eraseColor(Color.GREEN);
    AtomicInteger result = new AtomicInteger(-1);

    RuntimeException actual =
        assertThrows(
            RuntimeException.class,
            () ->
                PixelCopy.request(
                    source, destination, result::set, new Handler(Looper.getMainLooper())));

    assertThat(actual).isSameInstanceAs(failure);
    assertThat(source.intermediate.isRecycled()).isTrue();
    assertDetached(source.canvas);
    assertThat(destination.isRecycled()).isFalse();
    assertThat(destination.getPixel(0, 0)).isEqualTo(Color.GREEN);
    assertThat(result.get()).isEqualTo(-1);
  }

  @Test
  public void softwareCopy_unfinishedLayerCleanupFollowsCopy() {
    properties.set("robolectric.pixelCopyRenderMode", "software");
    RecordingSurfaceView source = new RecordingSurfaceView();
    source.leaveSavedLayer = true;
    Bitmap destination = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888);

    PixelCopy.request(
        source,
        destination,
        result -> assertThat(result).isEqualTo(PixelCopy.SUCCESS),
        new Handler(Looper.getMainLooper()));

    // The unfinished layer has no effect on the pixels copied before Canvas state is released.
    assertThat(destination.getPixel(3, 3)).isEqualTo(Color.RED);
    assertThat(source.intermediate.isRecycled()).isTrue();
    assertDetached(source.canvas);
  }

  @Test
  public void hardwareCopy_destinationRemainsReadable() {
    properties.set("robolectric.pixelCopyRenderMode", "hardware");
    try (var controller = Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      activity.getWindow().getDecorView().setBackgroundColor(Color.RED);
      assertThat(HardwareRenderingScreenshot.canTakeScreenshot(activity.getWindow().getDecorView()))
          .isTrue();
      Bitmap destination = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
      PixelCopy.request(
          activity.getWindow(),
          destination,
          result -> assertThat(result).isEqualTo(PixelCopy.SUCCESS),
          new Handler(Looper.getMainLooper()));
      assertThat(destination.isRecycled()).isFalse();
      assertThat(destination.getPixel(4, 4)).isEqualTo(Color.RED);
    }
  }

  /** Retaining the Java Canvas must not retain the bitmap or its native drawing target. */
  private static void assertDetached(Canvas canvas) {
    assertThat(ReflectionHelpers.<Bitmap>getField(canvas, "mBitmap")).isNull();
    assertThat(canvas.getWidth()).isEqualTo(0);
    assertThat(canvas.getHeight()).isEqualTo(0);
  }

  /** Exposes the Canvas target only to observe the ownership boundary after the copy. */
  private static final class RecordingSurfaceView extends SurfaceView {
    Bitmap intermediate;
    Canvas canvas;
    RuntimeException drawFailure;
    boolean leaveSavedLayer;

    RecordingSurfaceView() {
      super(RuntimeEnvironment.getApplication());
      layout(0, 0, 4, 4);
    }

    @Override
    public void draw(Canvas canvas) {
      this.canvas = canvas;
      intermediate = ReflectionHelpers.getField(canvas, "mBitmap");
      if (drawFailure != null) {
        throw drawFailure;
      }
      canvas.drawColor(Color.RED);
      if (leaveSavedLayer) {
        canvas.saveLayer(0, 0, 4, 4, null);
      }
      Paint paint = new Paint();
      paint.setColor(Color.BLUE);
      canvas.drawRect(2, 0, 4, 4, paint);
    }
  }
}
