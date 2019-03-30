package org.robolectric.android.internal;

import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;

import androidx.test.internal.platform.os.ControlledLooper;

public class LocalControlledLooper implements ControlledLooper {

  @Override
  public void drainMainThreadUntilIdle() {
    shadowMainLooper().idle();
  }
}
