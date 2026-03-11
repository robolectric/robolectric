package org.robolectric.rap.ksp;

import android.content.IntentFilter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Java shadow class processed alongside Kotlin shadows by the KSP processor. Tests that the KSP
 * processor correctly handles Java source files in a mixed Kotlin/Java module.
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
