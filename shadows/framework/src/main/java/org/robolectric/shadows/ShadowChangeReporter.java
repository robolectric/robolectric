package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import com.android.internal.compat.ChangeReporter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = ChangeReporter.class, isInAndroidSdk = false, minSdk = R)
public class ShadowChangeReporter {

  /** Don't write any compat change to logs, as its spammy in Robolectric. */
  @Implementation
  protected boolean shouldWriteToDebug(int uid, long changeId, int state) {
    return false;
  }
}
