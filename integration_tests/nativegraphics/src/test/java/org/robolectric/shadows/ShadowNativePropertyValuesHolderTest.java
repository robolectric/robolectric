package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;

import android.animation.PropertyValuesHolder;
import android.app.Instrumentation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePropertyValuesHolderTest {

  private Instrumentation instrumentation;
  private float startY;
  private float endY;
  private String property;

  @Before
  public void setup() {
    instrumentation = InstrumentationRegistry.getInstrumentation();
    instrumentation.setInTouchMode(false);
    property = "y";
    startY = 0;
    endY = 10;
  }

  @Test
  public void testGetPropertyName() {
    float[] values = {startY, endY};
    PropertyValuesHolder pVHolder = PropertyValuesHolder.ofFloat(property, values);
    assertEquals(property, pVHolder.getPropertyName());
  }

  @Test
  public void testSetPropertyName() {
    float[] values = {startY, endY};
    PropertyValuesHolder pVHolder = PropertyValuesHolder.ofFloat("", values);
    pVHolder.setPropertyName(property);
    assertEquals(property, pVHolder.getPropertyName());
  }

  @Test
  public void testClone() {
    float[] values = {startY, endY};
    PropertyValuesHolder pVHolder = PropertyValuesHolder.ofFloat(property, values);
    PropertyValuesHolder cloneHolder = pVHolder.clone();
    assertEquals(pVHolder.getPropertyName(), cloneHolder.getPropertyName());
  }
}
