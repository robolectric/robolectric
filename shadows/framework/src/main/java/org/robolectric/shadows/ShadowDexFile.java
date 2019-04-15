package org.robolectric.shadows;

import dalvik.system.DexFile;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow implementation of dalvik.system.Dexfile. */
@Implements(DexFile.class)
public class ShadowDexFile {
  private static boolean isDexOptNeeded = false;
  private static Throwable dexOptNeededError = null;

  @Implementation
  protected static boolean isDexOptNeeded(String fileName) throws Throwable {
    if (dexOptNeededError != null) {
      dexOptNeededError.fillInStackTrace();
      throw dexOptNeededError;
    }
    return isDexOptNeeded;
  }

  /** Sets the value to be returned when isDexOptNeeded() is called with any argument. */
  public static void setIsDexOptNeeded(boolean isDexOptNeeded) {
    ShadowDexFile.isDexOptNeeded = isDexOptNeeded;
  }

  /**
   * Sets the throwable that will be thrown when isDexOptNeeded() is called. isDexOptNeeded() won't
   * throw if the error is null.
   */
  public static void setIsDexOptNeededError(Throwable error) {
    dexOptNeededError = error;
  }

  @Resetter
  public static void reset() {
    isDexOptNeeded = false;
    dexOptNeededError = null;
  }
}
