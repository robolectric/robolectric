package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static com.google.common.truth.TruthJUnit.assume;

import android.graphics.RuntimeShader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.U;

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
    assume().that(OS_NAME.value().toLowerCase(Locale.US)).doesNotContain("mac");
  }

  @Config(minSdk = S, maxSdk = S_V2)
  @Test
  public void testConstructor() {
    var unused =
        ReflectionHelpers.callConstructor(
            RuntimeShader.class,
            ClassParameter.from(String.class, SKSL),
            ClassParameter.from(boolean.class, false));
  }

  /** {@link #SKSL} does not compile on V and above. */
  @Config(minSdk = TIRAMISU, maxSdk = U.SDK_INT)
  @Test
  public void testConstructorT() {
    var unused = new RuntimeShader(SKSL);
  }

  @Test
  public void rippleShader_ctor() throws Exception {
    ReflectionHelpers.callConstructor(Class.forName("android.graphics.drawable.RippleShader"));
  }
}
