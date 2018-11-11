package org.robolectric.shadows;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = TileService.class, minSdk = Build.VERSION_CODES.N)
public final class ShadowTileService {

  private Tile tile;

  @Implementation
  protected final Tile getQsTile() {
    if (tile == null) {
      tile = createTile();
    }
    return tile;
  }

  private static Tile createTile() {
    return Shadow.newInstanceOf(Tile.class);
  }
}
