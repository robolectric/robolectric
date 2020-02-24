package org.robolectric.shadows;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.os.IBinder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(WallpaperManager.class)
public class ShadowWallpaperManager {

  @Implementation
  protected void sendWallpaperCommand(
      IBinder windowToken, String action, int x, int y, int z, Bundle extras) {}
}
