package org.robolectric.android;

import static org.assertj.core.api.Assertions.assertThat;

import android.graphics.Bitmap;
import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = Config.NEWEST_SDK)
public class AndroidTranslatorClassInstrumentedTest {

  @Test
  @Config(shadows = ShadowPaintForTests.class)
  public void testNativeMethodsAreDelegated() throws Exception {
    Paint paint = new Paint();
    paint.setColor(1234);

    assertThat(paint.getColor()).isEqualTo(1234);
  }

  @Test
  @Config(shadows = ShadowClassWithPrivateConstructor.class)
  public void testClassesWithPrivateDefaultConstructorsCanBeShadowed() {
    ClassWithPrivateConstructor inst = new ClassWithPrivateConstructor();
    assertThat(inst.getInt()).isEqualTo(42);
  }

  @Test
  public void testEnumConstructorsAreNotRewritten() {
    // just referencing this enum value would blow up if we rewrite its constructor
    Bitmap.Config alpha8 = Bitmap.Config.ALPHA_8;
    assertThat(alpha8.toString()).isEqualTo("ALPHA_8");
  }

  /*
   * Test "foreign class" getting its methods shadowed whe it's
   * in the SandboxClassLoader CustomClassNames arrayList
   */
  @Test
  @Config(shadows = {ShadowCustomPaint.class, ShadowPaintForTests.class})
  public void testCustomMethodShadowed() throws Exception {
    CustomPaint customPaint = new CustomPaint();
    assertThat(customPaint.getColor()).isEqualTo(10);
    assertThat(customPaint.getColorName()).isEqualTo("rainbow");
  }

  @Instrument
  public static class ClassWithPrivateConstructor {
    private ClassWithPrivateConstructor() {
    }

    public int getInt() {
      return 99;
    }
  }

  @Implements(ClassWithPrivateConstructor.class)
  public static class ShadowClassWithPrivateConstructor {
    @Implementation
    public int getInt() {
      return 42;
    }
  }

  @Implements(Paint.class)
  public static class ShadowPaintForTests {
    private int color;

    @Implementation
    public void setColor(int color) {
      this.color = color;
    }

    @Implementation
    public int getColor() {
      return color;
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @Instrument
  public static class CustomPaint extends Paint {
    private int customColor;

    @Override
    public int getColor() {
      return customColor;
    }

    public String getColorName() {
      return Integer.toString(customColor);
    }
  }

  @Implements(CustomPaint.class)
  public static class ShadowCustomPaint extends ShadowPaintForTests {

    @Override
    @Implementation
    public int getColor() {
      return 10;
    }

    @Implementation
    public String getColorName() {
      return "rainbow";
    }
  }
}
