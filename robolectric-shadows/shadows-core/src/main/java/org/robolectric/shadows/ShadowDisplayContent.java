package org.robolectric.shadows;

import android.view.Display;
import com.android.server.wm.WindowManagerService;
import org.robolectric.annotation.Implements;

@Implements(className = "com.android.server.wm.DisplayContent", isInAndroidSdk = false)
public class ShadowDisplayContent {
  public void __constructor__(Display display, WindowManagerService service) {
  }
}
