package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

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
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

/**
 * Test for {@link org.robolectric.shadows.ShadowNativeImageReader} and {@link
 * org.robolectric.shadows.ShadowNativeImageReaderSurfaceImage}.
 */
@Config(minSdk = P)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeImageReaderTest {
  @Rule public SetSystemPropertyRule properties = new SetSystemPropertyRule();

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
    try (ImageReader reader = ImageReader.newInstance(100, 100, PixelFormat.RGBA_8888, 1)) {
      Surface surface = reader.getSurface();
      Canvas canvas = surface.lockHardwareCanvas();
      canvas.drawColor(Color.RED);
      surface.unlockCanvasAndPost(canvas);
      Image image = reader.acquireNextImage();
      assertThat(image).isNotNull();
      Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
      Plane[] planes = image.getPlanes();
      assertThat(planes[0].getBuffer().get(0) & 0xff).isEqualTo(255);
      assertThat(planes[0].getBuffer().get(2) & 0xff).isEqualTo(0);
      assertThat(image.getPlanes()[0].getBuffer()).isSameInstanceAs(planes[0].getBuffer());
      bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
      assertThat(Integer.toHexString(bitmap.getPixel(50, 50)))
          .isEqualTo(Integer.toHexString(Color.RED));
    }
  }

  @Test
  public void imageReader_rawCopyPreservesPixelsAndPublicPlanes() {
    try (ImageReader reader = ImageReader.newInstance(2, 2, PixelFormat.RGBA_8888, 1)) {
      Surface surface = reader.getSurface();
      try {
        Canvas canvas = surface.lockHardwareCanvas();
        canvas.drawColor(Color.RED);
        surface.unlockCanvasAndPost(canvas);
        try (Image image = reader.acquireNextImage()) {
          ByteBuffer raw =
              reflector(ShadowNativeImageReaderSurfaceImage.SurfaceImageReflector.class, image)
                  .getPlanes()[0]
                  .getBuffer();
          int position = raw.position();
          Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);

          ShadowNativeImageReaderSurfaceImage.copyPixelsToBitmap(image, bitmap);

          assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.RED);
          assertThat(raw.position()).isEqualTo(position);
          ByteBuffer rgba = image.getPlanes()[0].getBuffer();
          assertThat(rgba.get(0) & 0xff).isEqualTo(255);
          assertThat(rgba.get(2) & 0xff).isEqualTo(0);
        }
      } finally {
        surface.release();
      }
    }
  }

  @Test
  public void imageReader_rgbaPixelOrderDisabledUsesNativePlanes() {
    properties.set("robolectric.useRgbaPixelOrder", "false");
    try (ImageReader reader = ImageReader.newInstance(2, 2, PixelFormat.RGBA_8888, 1)) {
      Surface surface = reader.getSurface();
      try {
        Canvas canvas = surface.lockHardwareCanvas();
        canvas.drawColor(Color.RED);
        surface.unlockCanvasAndPost(canvas);
        try (Image image = reader.acquireNextImage()) {
          ByteBuffer raw =
              reflector(ShadowNativeImageReaderSurfaceImage.SurfaceImageReflector.class, image)
                  .getPlanes()[0]
                  .getBuffer();
          ByteBuffer pixels = image.getPlanes()[0].getBuffer();
          assertThat(pixels).isSameInstanceAs(raw);
          Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);

          bitmap.copyPixelsFromBuffer(pixels.duplicate());

          assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.RED);
          ShadowNativeImageReaderSurfaceImage.copyPixelsToBitmap(image, bitmap);
          assertThat(bitmap.getPixel(0, 0)).isEqualTo(Color.RED);

          properties.set("robolectric.useRgbaPixelOrder", "true");
          ByteBuffer rgba = image.getPlanes()[0].getBuffer();
          assertThat(rgba.get(0) & 0xff).isEqualTo(255);
          assertThat(rgba.get(2) & 0xff).isEqualTo(0);
        }
      } finally {
        surface.release();
      }
    }
  }

  @Test
  public void testGetHardwareBuffer() {
    try (ImageReader reader = ImageReader.newInstance(1, 1, PixelFormat.RGBA_8888, 1)) {
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
}
