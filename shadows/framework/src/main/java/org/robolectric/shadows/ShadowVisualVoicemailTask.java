package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.telephony.VisualVoicemailService.VisualVoicemailTask;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link VisualVoicemailTask}. */
@Implements(value = VisualVoicemailTask.class, minSdk = VERSION_CODES.O)
public class ShadowVisualVoicemailTask {

  private boolean isFinished;

  @Implementation
  public void finish() {
    isFinished = true;
  }

  public boolean isFinished() {
    return isFinished;
  }
}
