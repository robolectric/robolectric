package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.HardwareRenderer;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.graphics.RuntimeShader;
import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.OsUtil;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Config(minSdk = S)
@RunWith(AndroidJUnit4.class)
public class ShadowNativeRuntimeShaderTest {
  static final String SKSL =
      ""
          + "uniform float2 in_origin;"
          + "uniform float in_progress;\n"
          + "uniform float in_maxRadius;\n"
          + "uniform shader in_paintColor;\n"
          + "float dist2(float2 p0, float2 pf) { return sqrt((pf.x - p0.x) * (pf.x - p0.x) + "
          + "(pf.y - p0.y) * (pf.y - p0.y)); }\n"
          + "float mod2(float a, float b) { return a - (b * floor(a / b)); }\n"
          + "float rand(float2 src) { return fract(sin(dot(src.xy, float2(12.9898, 78.233)))"
          + " * 43758.5453123); }\n"
          + "float4 main(float2 p)\n"
          + "{\n"
          + "    float fraction = in_progress;\n"
          + "    float2 fragCoord = p;//sk_FragCoord.xy;\n"
          + "    float maxDist = in_maxRadius;\n"
          + "    float fragDist = dist2(in_origin, fragCoord.xy);\n"
          + "    float circleRadius = maxDist * fraction;\n"
          + "    float colorVal = (fragDist - circleRadius) / maxDist;\n"
          + "    float d = fragDist < circleRadius \n"
          + "        ? 1. - abs(colorVal * 2. * smoothstep(0., 1., fraction)) \n"
          + "        : 1. - abs(colorVal * 3.);\n"
          + "    d = smoothstep(0., 1., d);\n"
          + "    float divider = 2.;\n"
          + "    float x = floor(fragCoord.x / divider);\n"
          + "    float y = floor(fragCoord.y / divider);\n"
          + "    float density = .95;\n"
          + "    d = rand(float2(x, y)) > density ? d : d * .2;\n"
          + "    d = d * rand(float2(fraction, x * y));\n"
          + "    float alpha = 1. - pow(fraction, 3.);\n"
          + "    return float4(sample(in_paintColor, p).rgb, d * alpha);\n"
          + "}";

  @Before
  public void setup() {
    // The native code behind RuntimeShader is currently not supported on Mac.
    assume().that(OsUtil.isMac()).isFalse();
  }

  @Config(minSdk = S, maxSdk = S_V2)
  @Test
  public void testConstructor() {
    ReflectionHelpers.callConstructor(
        RuntimeShader.class,
        ClassParameter.from(String.class, SKSL),
        ClassParameter.from(boolean.class, false));
  }

  /** {@link #SKSL} does not compile on V and above. */
  @SuppressWarnings("CheckReturnValue")
  @Config(minSdk = TIRAMISU, maxSdk = UPSIDE_DOWN_CAKE)
  @Test
  public void testConstructorT() {
    new RuntimeShader(SKSL);
  }

  @Test
  public void rippleShader_ctor() throws Exception {
    ReflectionHelpers.callConstructor(Class.forName("android.graphics.drawable.RippleShader"));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setFloatUniform_scalarValues_areUsedByShader() {
    RuntimeShader shader =
        new RuntimeShader(
            "uniform float in_red;\n"
                + "uniform float2 in_green;\n"
                + "uniform float3 in_blue;\n"
                + "uniform float4 in_alpha;\n"
                + "half4 main(float2 p) {\n"
                + "  return half4(in_red, in_green.y, in_blue.z, in_alpha.w);\n"
                + "}");
    // Set every uniform with the array setter first, so that a scalar setter that does nothing
    // leaves a known color instead of whatever the uniform memory held.
    shader.setFloatUniform("in_red", new float[] {0});
    shader.setFloatUniform("in_green", new float[] {0, 0});
    shader.setFloatUniform("in_blue", new float[] {0, 0, 0});
    shader.setFloatUniform("in_alpha", new float[] {0, 0, 0, 0});
    assertThat(Integer.toHexString(drawPixel(shader)))
        .isEqualTo(Integer.toHexString(Color.TRANSPARENT));

    shader.setFloatUniform("in_red", 1);
    shader.setFloatUniform("in_green", 0, 1);
    shader.setFloatUniform("in_blue", 0, 0, 1);
    shader.setFloatUniform("in_alpha", 0, 0, 0, 1);
    assertThat(Integer.toHexString(drawPixel(shader))).isEqualTo(Integer.toHexString(Color.WHITE));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void setIntUniform_scalarValues_areUsedByShader() {
    RuntimeShader shader =
        new RuntimeShader(
            "uniform int in_red;\n"
                + "uniform int2 in_green;\n"
                + "uniform int3 in_blue;\n"
                + "uniform int4 in_alpha;\n"
                + "half4 main(float2 p) {\n"
                + "  return half4(\n"
                + "      half(in_red), half(in_green.y), half(in_blue.z), half(in_alpha.w));\n"
                + "}");
    shader.setIntUniform("in_red", new int[] {0});
    shader.setIntUniform("in_green", new int[] {0, 0});
    shader.setIntUniform("in_blue", new int[] {0, 0, 0});
    shader.setIntUniform("in_alpha", new int[] {0, 0, 0, 0});
    assertThat(Integer.toHexString(drawPixel(shader)))
        .isEqualTo(Integer.toHexString(Color.TRANSPARENT));

    shader.setIntUniform("in_red", 1);
    shader.setIntUniform("in_green", 0, 1);
    shader.setIntUniform("in_blue", 0, 0, 1);
    shader.setIntUniform("in_alpha", 0, 0, 0, 1);
    assertThat(Integer.toHexString(drawPixel(shader))).isEqualTo(Integer.toHexString(Color.WHITE));
  }

  /** Draws {@code shader} with hardware rendering, which RuntimeShader requires from T. */
  private static int drawPixel(RuntimeShader shader) {
    try (ImageReader imageReader = ImageReader.newInstance(1, 1, PixelFormat.RGBA_8888, 1)) {
      RenderNode renderNode = new RenderNode("RuntimeShaderNode");
      renderNode.setPosition(0, 0, 1, 1);
      RecordingCanvas canvas = renderNode.beginRecording();
      Paint paint = new Paint();
      paint.setShader(shader);
      canvas.drawPaint(paint);
      renderNode.endRecording();
      HardwareRenderer renderer = new HardwareRenderer();
      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      renderer.setContentRoot(renderNode);
      renderer.createRenderRequest().syncAndDraw();
      try (Image image = imageReader.acquireNextImage()) {
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(image.getPlanes()[0].getBuffer());
        return bitmap.getPixel(0, 0);
      } finally {
        renderer.destroy();
        surface.release();
      }
    }
  }
}
