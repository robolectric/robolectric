package org.robolectric.shadows;

import android.os.UserHandle;
import android.os.UserManager;
import java.util.List;
import org.robolectric.RuntimeEnvironment;

/** Delegate for {@link ILauncherApps}. */
class LauncherAppsDelegate {

  public List<UserHandle> getUserProfiles() {
    return RuntimeEnvironment.getApplication()
        .getSystemService(UserManager.class)
        .getUserProfiles();
  }
}
