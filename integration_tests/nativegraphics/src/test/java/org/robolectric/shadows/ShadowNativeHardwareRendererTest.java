package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.ColorInt;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.HardwareRenderer;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.view.Choreographer;
import android.view.Surface;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeHardwareRendererTest {

  @Test
  public void test_hardwareRenderer() {
    HardwareRenderer unused = new HardwareRenderer();
  }

  @Config(maxSdk = R)
  @Test
  public void setWideGamut_doesNotCrash() {
    HardwareRenderer renderer = new HardwareRenderer();
    reflector(HardwareRendererReflector.class, renderer).setWideGamut(true);
    reflector(HardwareRendererReflector.class, renderer).setWideGamut(false);
  }

  @Test
  public void choreographer_firstCalled() {
    // In some SDK levels, the Choreographer constructor ends up calling
    // HardwareRenderer.nHackySetRTAnimationsEnabled. Ensure that RNG is loaded if this happens.
    var unused = Choreographer.getInstance();
  }

  @Test
  @Config(minSdk = Q)
  public void imageReader_readsRenderedDisplayList() {
    // This API is not supported correctly on macOS now.
    assume()
        .that(
            Objects.requireNonNull(System.getProperty("os.name"))
                .toLowerCase(Locale.US)
                .contains("mac"))
        .isFalse();
    int width = 100;
    int height = 100;

    try (ImageReader imageReader =
        ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)) {
      HardwareRenderer renderer = new HardwareRenderer();
      RenderNode renderNode = new RenderNode("RedNode");
      renderNode.setPosition(0, 0, width, height);
      RecordingCanvas canvas = renderNode.beginRecording();
      canvas.drawColor(Color.RED);
      renderNode.endRecording();
      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      renderer.setContentRoot(renderNode);
      renderer.createRenderRequest().syncAndDraw();
      Image nativeImage = imageReader.acquireNextImage();
      Plane[] planes = nativeImage.getPlanes();
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
      assertThat(Integer.toHexString(bitmap.getPixel(50, 50)))
          .isEqualTo(Integer.toHexString(Color.RED));
    }
  }

  @Test
  public void hardwareRenderer_drawDisplayList_validateARGB() {
    int pw = 320;
    int ph = 470;

    try (ImageReader imageReader = ImageReader.newInstance(pw, ph, PixelFormat.RGBA_8888, 1)) {
      // Note on pixel format:
      // - ImageReader is configured as RGBA_8888 above.
      // - However the native libs/hwui/pipeline/skia/SkiaHostPipeline.cpp always treats
      //   the buffer as BGRA_8888 on Linux and Windows, or RGBA_8888 n Mac.

      HardwareRenderer renderer = new HardwareRenderer();
      RenderNode displayList = createDisplayList(pw, ph);

      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      Image nativeImage = imageReader.acquireNextImage();
      renderer.setContentRoot(displayList);
      renderer.createRenderRequest().syncAndDraw();

      int[] dstImageData = new int[pw * ph];
      Plane[] planes = nativeImage.getPlanes();
      IntBuffer buff = planes[0].getBuffer().order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
      buff.get(dstImageData);

      // The image drawn is here:
      // https://gist.github.com/hoisie/2c78e5e86ce335f7c5a2431f72a92888#file-hw_render-png

      // Check that the pixel at (0, 0) is white.
      assertThat(Integer.toHexString(dstImageData[0])).isEqualTo("ffffffff");
      if (isMac()) {
        // Check for red pixels in ABGR format on Mac.
        assertThat(Integer.toHexString(dstImageData[1])).isEqualTo("ff0000ff");
        assertThat(Integer.toHexString(dstImageData[2])).isEqualTo("ff0000ff");
      } else {
        // Check for red pixels in ARGB format on Linux/Windows.
        assertThat(Integer.toHexString(dstImageData[1])).isEqualTo("ffff0000");
        assertThat(Integer.toHexString(dstImageData[2])).isEqualTo("ffff0000");
      }
      surface.release();
    }
  }

  /**
   * This function draws three overlapping rectangles: a red one in the top left quadrant, a green
   * on centered in the middle, and a blue one in the bottom right quadrant. Each of the rectangles
   * has a shadow, and the bottom right one also has a round corner. It also contains a white point
   * at the top left.
   *
   * <p>You can see the image drawn here:
   * https://gist.github.com/hoisie/2c78e5e86ce335f7c5a2431f72a92888#file-hw_render-png
   */
  private static RenderNode createDisplayList(int width, int height) {
    RenderNode renderNode = new RenderNode("MyRenderNode");
    renderNode.setPosition(0, 0, width, height);
    RecordingCanvas canvas = renderNode.beginRecording();
    // TODO(hoisie): simplify this drawing logic.
    int w4 = width / 4;
    int h4 = height / 4;
    try {
      // Draw a red rectangle in the top left quadrant.
      canvas.drawRect(0, 0, 2 * w4, 2 * h4, createPaint(Color.RED));
      // Draw a green rectangle in the center.
      canvas.drawRect(w4, h4, 3 * w4, 3 * h4, createPaint(Color.GREEN));
      // Draw a rounded blue rectangle in the bottom right quadrant.
      canvas.drawRoundRect(
          2 * w4, 2 * h4, width, height, width / 10f, height / 10f, createPaint(Color.BLUE));
      // Draw a white point at the top left.
      canvas.drawPoint(0.5f, 0.5f, createPaint(Color.WHITE));
    } finally {
      renderNode.endRecording();
    }
    return renderNode;
  }

  private static Paint createPaint(@ColorInt int color) {
    Paint paint = new Paint();
    paint.setColor(color);
    paint.setShadowLayer(20.f, 5.f, 5.f, color & 0xCFFFFFFF);
    return paint;
  }

  @ForType(HardwareRenderer.class)
  interface HardwareRendererReflector {
    void setWideGamut(boolean wideGamut);
  }

  private static boolean isMac() {
    return OS_NAME.value().toLowerCase(Locale.ROOT).contains("mac");
  }
}
