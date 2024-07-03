package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Test for {@link org.robolectric.shadows.ShadowNativeImageReader} and {@link
 * org.robolectric.shadows.ShadowNativeImageReaderSurfaceImage}.
 */
@Config(minSdk = P)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeImageReaderTest {

  private static final int SX = 320;
  private static final int SY = 320;

  @Test
  public void test_imageReader_newInstance_validateHasSurface() {
    try (ImageReader imageReader = ImageReader.newInstance(SX, SY, PixelFormat.RGBA_8888, 1)) {

      assertThat(imageReader).isNotNull();
      Surface surface = imageReader.getSurface();
      try {
        assertThat(surface).isNotNull();
        assertThat(surface.getClass().getSimpleName()).doesNotContain("Fake");
        assertThat(surface.isValid()).isTrue();
      } finally {
        surface.release();
      }
    }
  }

  @Test
  public void test_imageReader_getSurface_acquireNextImage() {
    try (ImageReader imageReader = ImageReader.newInstance(SX, SY, PixelFormat.RGBA_8888, 1)) {

      Surface surface = imageReader.getSurface();
      try {
        assertThat(surface).isNotNull();
        // Note: do not call surface.lockCanvas(), it is not implemented on the JNI side.

        try (Image image = imageReader.acquireNextImage()) {
          assertThat(image).isNotNull();
          assertThat(image.getFormat()).isEqualTo(PixelFormat.RGBA_8888);
          assertThat(image.getWidth()).isEqualTo(320);
          assertThat(image.getHeight()).isEqualTo(320);
          assertThat(image.getPlanes()).hasLength(1);
        }
      } finally {
        surface.release();
      }
    }
  }

  @Test
  public void test_imageReader_getSurface_lockCanvas() {
    try (ImageReader imageReader = ImageReader.newInstance(SX, SY, PixelFormat.RGBA_8888, 1)) {
      Surface surface = imageReader.getSurface();
      Canvas canvas = surface.lockCanvas(new Rect(0, 0, SX, SY));
      surface.unlockCanvasAndPost(canvas);
    }
  }

  @Test
  public void imageReader_setOnImageAvailableListener() {
    try (ImageReader imageReader = ImageReader.newInstance(SX, SY, PixelFormat.RGBA_8888, 1)) {
      AtomicBoolean onImageAvailableInvoked = new AtomicBoolean();
      imageReader.setOnImageAvailableListener(
          reader -> onImageAvailableInvoked.set(true), new Handler(Looper.getMainLooper()));
      Surface surface = imageReader.getSurface();
      Canvas canvas = surface.lockHardwareCanvas();
      surface.unlockCanvasAndPost(canvas);
      shadowOf(Looper.getMainLooper()).idle();
      assertThat(onImageAvailableInvoked.get()).isTrue();
    }
  }

  @Test
  public void imageReader_lockHardwareCanvas_drawColor() {
    ImageReader reader = ImageReader.newInstance(100, 100, PixelFormat.RGBA_8888, 1);
    Surface surface = reader.getSurface();
    Canvas canvas = surface.lockHardwareCanvas();
    canvas.drawColor(Color.RED);
    surface.unlockCanvasAndPost(canvas);
    Image image = reader.acquireNextImage();
    assertThat(image).isNotNull();
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Plane[] planes = image.getPlanes();
    bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
    surface.release();
    assertThat(Integer.toHexString(bitmap.getPixel(50, 50)))
        .isEqualTo(Integer.toHexString(Color.RED));
  }

  @Test
  public void testGetHardwareBuffer() throws Exception {
    ImageReader reader = ImageReader.newInstance(1, 1, PixelFormat.RGBA_8888, 1);
    Surface surface = reader.getSurface();
    Canvas canvas = surface.lockHardwareCanvas();
    canvas.drawColor(Color.RED);
    surface.unlockCanvasAndPost(canvas);
    Image image = reader.acquireNextImage();
    assertThat(image).isNotNull();
    HardwareBuffer buffer = image.getHardwareBuffer();
    // TODO(hoisie): buffer should not be null, but fixing it will require an implementation of
    // HardwareBuffer on host libandroid_runtime.
    assertThat(buffer).isNull();
  }
}
