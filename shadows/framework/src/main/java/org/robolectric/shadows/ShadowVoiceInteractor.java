package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.Q;

import android.app.VoiceInteractor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow implementation of {@link android.app.VoiceInteractor}. */
@Implements(value = VoiceInteractor.class, minSdk = M)
public class ShadowVoiceInteractor {

  private int directActionsInvalidationCount = 0;

  @Implementation(minSdk = Q)
  protected void notifyDirectActionsChanged() {
    directActionsInvalidationCount += 1;
  }

  /**
   * Returns the number of times {@code notifyDirectActionsChanged} was called on the {@link
   * android.app.VoiceInteractor} instance associated with this shadow
   */
  public int getDirectActionsInvalidationCount() {
    return directActionsInvalidationCount;
  }
}
