package org.robolectric.bytecode;

import android.graphics.Bitmap;
import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithCustomClassList.class)
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
   * in the InstrumentingClassLoader CustomClassNames arrayList
   */
  @Test
  @Config(shadows = {ShadowCustomPaint.class, ShadowPaintForTests.class})
  public void testCustomMethodShadowed() throws Exception {
    CustomPaint customPaint = new CustomPaint();
    assertThat(customPaint.getColor()).isEqualTo(10);
    assertThat(customPaint.getColorName()).isEqualTo("rainbow");
  }

  /*
   * Test "foreign class" not getting its methods shadowed when it's
   * not in the InstrumentingClassLoader CustomClassNames arrayList
   */
  @Test
  @Config(shadows = {ShadowCustomXmasPaint.class, ShadowPaintForTests.class})
  public void testCustomMethodNotShadowed() throws Exception {
    CustomXmasPaint customXmasPaint = new CustomXmasPaint();
    assertThat(customXmasPaint.getColor()).isEqualTo(999);
    assertThat(customXmasPaint.getColorName()).isEqualTo("XMAS");
  }

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

    @Implementation
    public int getColor() {
      return 10;
    }

    @Implementation
    public String getColorName() {
      return "rainbow";
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static class CustomXmasPaint extends Paint {

    @Override
    public int getColor() {
      return 999;
    }

    public String getColorName() {
      return "XMAS";
    }
  }

  @Implements(CustomXmasPaint.class)
  public static class ShadowCustomXmasPaint extends ShadowPaintForTests {

    @Implementation
    public int getColor() {
      return -999;
    }

    @Implementation
    public String getColorName() {
      return "XMAS Color Test";
    }
  }
}
