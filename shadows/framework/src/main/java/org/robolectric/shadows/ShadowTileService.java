package org.robolectric.shadows;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(value = TileService.class, minSdk = Build.VERSION_CODES.N)
public class ShadowTileService {

  private Tile tile;
  private boolean isLocked = false;
  @RealObject private TileService realObject;

  @Implementation
  protected final Tile getQsTile() {
    if (tile == null) {
      tile = createTile();
    }
    return tile;
  }

  @Implementation
  protected final void unlockAndRun(Runnable runnable) {
    setLocked(false);
    if (runnable != null) {
      runnable.run();
    }
  }

  /** Starts an activity without collapsing the quick settings panel. */
  @Implementation
  protected void startActivityAndCollapse(Intent intent) {
    realObject.startActivity(intent);
  }

  @Implementation
  protected boolean isLocked() {
    return isLocked;
  }

  public void setLocked(boolean locked) {
    this.isLocked = locked;
  }

  private static Tile createTile() {
    return Shadow.newInstanceOf(Tile.class);
  }
}
