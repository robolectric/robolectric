package org.robolectric.shadows;

import android.app.WallpaperManager;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(WallpaperManager.class)
public class ShadowWallpaperManager {

  @Implementation
  protected static WallpaperManager getInstance(Context context) {
    return Shadow.newInstanceOf(WallpaperManager.class);
  }

  @Implementation
  protected void sendWallpaperCommand(
      IBinder windowToken, String action, int x, int y, int z, Bundle extras) {}
}
