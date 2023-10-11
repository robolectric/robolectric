package org.robolectric.shadows;

import com.android.internal.compat.ChangeReporter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.versioning.AndroidVersions.R;

@Implements(value = ChangeReporter.class, isInAndroidSdk = false, minSdk = R.SDK_INT)
public class ShadowChangeReporter {

  /** Don't write any compat change to logs, as its spammy in Robolectric. */
  @Implementation
  protected boolean shouldWriteToDebug(int uid, long changeId, int state) {
    return false;
  }
}
