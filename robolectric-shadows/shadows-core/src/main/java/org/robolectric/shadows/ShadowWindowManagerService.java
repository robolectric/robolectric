package org.robolectric.shadows;

import android.view.Display;
import com.android.server.wm.WindowManagerService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = WindowManagerService.class, isInAndroidSdk = false)
public class ShadowWindowManagerService {
  @Implementation
  public void createDisplayContentLocked(Display display) {
  }
}
