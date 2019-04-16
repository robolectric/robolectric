package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for RotationWatcher for API 16-22
 */
@Implements(className = "com.android.internal.policy.impl.PhoneWindow$RotationWatcher",
    isInAndroidSdk = false, maxSdk = LOLLIPOP_MR1, looseSignatures = true)
public class ShadowRotationWatcherFor22 {

  @Implementation
  protected void addWindow(Object phoneWindow) {
    // ignore
  }
}
