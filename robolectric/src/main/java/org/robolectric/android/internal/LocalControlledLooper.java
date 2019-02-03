package org.robolectric.android.internal;

import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import androidx.test.internal.platform.os.ControlledLooper;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowBaseLooper;

public class LocalControlledLooper implements ControlledLooper {

  @Override
  public void drainMainThreadUntilIdle() {
    ShadowBaseLooper baseLooper = Shadow.extract(Looper.getMainLooper());
    baseLooper.idle();
  }
}
