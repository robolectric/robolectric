package org.robolectric.bytecode;

import android.graphics.Bitmap;
import android.graphics.Paint;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithCustomClassList.class)
public class AndroidTranslatorClassInstrumentedTest {

    @Test
    public void testNativeMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowPaintForTests.class);

        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor(), is(1234));
    }

    @Test
    public void testClassesWithPrivateDefaultConstructorsCanBeShadowed() {
        Robolectric.bindShadowClass(ShadowClassWithPrivateConstructor.class);

        ClassWithPrivateConstructor inst = new ClassWithPrivateConstructor();
        assertThat(inst.getInt(), is(42));
    }

    @Test
    public void testEnumConstructorsAreNotRewritten() {
        // just referencing this enum value would blow up if we rewrite its constructor
        Bitmap.Config alpha8 = Bitmap.Config.ALPHA_8;
        assertThat(alpha8.toString(), equalTo("ALPHA_8"));
    }

    /*
     * Test "foreign class" getting its methods shadowed whe it's
     * in the InstrumentingClassLoader CustomClassNames arrayList
     */
    @Test
    public void testCustomMethodShadowed() throws Exception {
        Robolectric.bindShadowClass(ShadowCustomPaint.class);

        CustomPaint customPaint = new CustomPaint();
        assertThat(customPaint.getColor(), equalTo(10));
        assertThat(customPaint.getColorName(), equalTo("rainbow"));
    }

    /*
     * Test "foreign class" not getting its methods shadowed when it's
     * not in the InstrumentingClassLoader CustomClassNames arrayList
     */
    @Test
    public void testCustomMethodNotShadowed() throws Exception {
        Robolectric.bindShadowClass(ShadowCustomXmasPaint.class);

        CustomXmasPaint customXmasPaint = new CustomXmasPaint();
        assertThat(customXmasPaint.getColor(), equalTo(999));
        assertThat(customXmasPaint.getColorName(), equalTo("XMAS"));
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
    public static class ShadowCustomPaint {

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
    public static class ShadowCustomXmasPaint {

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
