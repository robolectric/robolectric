package org.robolectric.rap.ksp;

import android.content.IntentFilter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Test fixture shadow used only by the rap-ksp integration tests. Written in Java to verify that
 * the KSP processor handles Java sources alongside Kotlin shadows in a mixed module.
 */
@Implements(value = IntentFilter.class)
public class ShadowIntentFilterJava {

  private static boolean resetCalled = false;

  public static boolean wasResetCalled() {
    return resetCalled;
  }

  @Resetter
  public static void reset() {
    resetCalled = true;
  }
}
