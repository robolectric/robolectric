package org.robolectric.shadows;

import com.android.internal.policy.PhoneWindow;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for RotationWatcher for API 23+ */
@Implements(
    className = "com.android.internal.policy.PhoneWindow$RotationWatcher",
    isInAndroidSdk = false)
public class ShadowRotationWatcher {

  @Implementation
  protected void addWindow(PhoneWindow phoneWindow) {
    // ignore
  }
}
