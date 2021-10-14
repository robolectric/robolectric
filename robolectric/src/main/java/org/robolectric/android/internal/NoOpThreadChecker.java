package org.robolectric.android.internal;

import androidx.test.internal.platform.ThreadChecker;

/**
 * In Robolectric environment, everything is executed on the main thread except for when you
 * manually create and run your code on worker thread.
 */
@SuppressWarnings("RestrictTo")
public class NoOpThreadChecker implements ThreadChecker {
  @Override
  public void checkMainThread() {}

  @Override
  public void checkNotMainThread() {}
}
