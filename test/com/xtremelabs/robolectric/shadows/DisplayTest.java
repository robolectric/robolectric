package com.xtremelabs.robolectric.shadows;

import android.util.DisplayMetrics;
import android.view.Display;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class DisplayTest {
    @Test
    public void shouldProvideDisplayMetrics() throws Exception {

        Display display = Robolectric.newInstanceOf(Display.class);
        ShadowDisplay shadow = Robolectric.shadowOf(display);

        shadow.setDensity(1.5f);
        shadow.setDensityDpi(DisplayMetrics.DENSITY_MEDIUM);
        shadow.setScaledDensity(1.6f);
        shadow.setWidth(1024);
        shadow.setHeight(600);
        shadow.setXdpi(183.0f);
        shadow.setYdpi(184.0f);

        DisplayMetrics metrics = new DisplayMetrics();

        display.getMetrics(metrics);

        assertEquals(1.5f, metrics.density, 0.05);
        assertEquals(DisplayMetrics.DENSITY_MEDIUM, metrics.densityDpi);
        assertEquals(1.6f, metrics.scaledDensity, 0.05);
        assertEquals(1024, metrics.widthPixels);
        assertEquals(600, metrics.heightPixels);
        assertEquals(183.0f, metrics.xdpi, 0.05);
        assertEquals(184.0f, metrics.ydpi, 0.05);
    }

}
