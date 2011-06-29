package com.xtremelabs.robolectric.bytecode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Paint;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithCustomClassListTestRunner;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@RunWith(WithCustomClassListTestRunner.class)
public class AndroidTranslatorClassIntrumentedTest {

    @Test
    public void testNativeMethodsAreDelegated() throws Exception {
        Robolectric.bindShadowClass(ShadowPaintForTests.class);

        Paint paint = new Paint();
        paint.setColor(1234);

        assertThat(paint.getColor(), is(1234));
    }
    
    /*
     * Test "foreign class" getting its methods shadowed whe it's
     * in the RobolectricClassLoader CustomClassNames arrayList
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
     * not in the RobolectricClassLoader CustomClassNames arrayList
     */
    @Test
    public void testCustomMethodNotShadowed() throws Exception {
    	Robolectric.bindShadowClass(ShadowCustomXmasPaint.class);
    	
    	CustomXmasPaint customXmasPaint = new CustomXmasPaint();
    	assertThat(customXmasPaint.getColor(), equalTo(999));
    	assertThat(customXmasPaint.getColorName(), equalTo("XMAS"));   	
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
    public static class ShadowCustomPaint{
    	
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
    public static class ShadowCustomXmasPaint{
    	
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
