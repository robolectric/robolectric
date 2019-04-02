package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.graphics.Canvas;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Tests for ShadowNativeAllocationRegistry
 */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public class ShadowNativeAllocationRegistryTest {

  private ShadowNativeAllocationRegistry registry;
  private final int cNullPtr = 0;

  public void setNativeRegisteredFlag(boolean newValue) {
    try {
      Canvas.class.getDeclaredField("$$robo_native_registered_flag$$").setBoolean(null, newValue);
    } catch (IllegalAccessException e) {
      throw new AssertionError("$$robo_native_registered_flag$$ must be public", e);
    } catch (NoSuchFieldException e) {
      throw new AssertionError(
          "Class must have native methods and instrumented"
              + " with $$robo_native_registered_flag$$", e);
    }
  }

  @Before
  public void setUp() {
    registry = new ShadowNativeAllocationRegistry();
  }

  @Test
  public void registerObjectWithNoNativeRegistered() {
    setNativeRegisteredFlag(false);
    Runnable r = registry.registerNativeAllocation(new Canvas(), cNullPtr);
    r.run();
  }

  @Test(expected = IllegalArgumentException.class)
  public void registerObjectWithNativeRegistered() {
    setNativeRegisteredFlag(true);
    // Should throw when a nullptr is passed in and is using the real NativeAllocationRegistry
    registry.registerNativeAllocation(new Canvas(), cNullPtr);
  }

  @After
  public void cleanUp(){
    setNativeRegisteredFlag(false);
  }
}
