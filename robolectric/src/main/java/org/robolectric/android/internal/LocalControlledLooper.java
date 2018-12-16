package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import androidx.test.internal.platform.os.ControlledLooper;

public class LocalControlledLooper implements ControlledLooper {

  @Override
  public void drainMainThreadUntilIdle() {
    shadowOf(Looper.getMainLooper()).idle();
  }
}
