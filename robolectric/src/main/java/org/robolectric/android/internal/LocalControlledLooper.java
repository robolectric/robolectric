package org.robolectric.android.internal;

import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import androidx.test.internal.platform.os.ControlledLooper;

public class LocalControlledLooper implements ControlledLooper {

  @Override
  public void drainMainThreadUntilIdle() {
    shadowMainLooper().idle();
  }
}
