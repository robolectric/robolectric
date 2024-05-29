package org.robolectric.integrationtests.nativegraphics;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

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
import java.util.Locale;
import java.util.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeHardwareRendererTest {

  @Test
  public void test_hardwareRenderer() {
    HardwareRenderer unused = new HardwareRenderer();
  }

  @Test
  public void choreographer_firstCalled() {
    // In some SDK levels, the Choreographer constructor ends up calling
    // HardwareRenderer.nHackySetRTAnimationsEnabled. Ensure that RNG is loaded if this happens.
    var unused = Choreographer.getInstance();
  }

  @Test
  @Config(minSdk = S)
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
      RenderNode displayList = createDisplayList(width, height);
      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      Image nativeImage = imageReader.acquireNextImage();
      renderer.setContentRoot(displayList);
      renderer.createRenderRequest().syncAndDraw();
      Plane[] planes = nativeImage.getPlanes();
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
      assertThat(bitmap.getPixel(50, 50)).isEqualTo(Color.RED);
    }
  }

  private static RenderNode createDisplayList(int width, int height) {
    RenderNode renderNode = new RenderNode("RedNode");
    renderNode.setPosition(0, 0, width, height);
    RecordingCanvas canvas = renderNode.beginRecording();
    Paint paint = new Paint();
    paint.setColor(Color.RED);
    canvas.drawRect(0, 0, width, height, paint);
    renderNode.endRecording();
    return renderNode;
  }
}
