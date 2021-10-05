package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;

import android.app.role.RoleControllerManager;
import android.content.ComponentName;
import android.content.Context;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = RoleControllerManager.class, minSdk = Q)
public class ShadowRoleControllerManager {

  @Implementation(minSdk = S)
  protected static ComponentName getRemoteServiceComponentName(Context context) {
    return new ComponentName("org.robolectric", "FakeRoleControllerManagerService");
  }
}
