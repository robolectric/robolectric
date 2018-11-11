package org.robolectric.shadows;

import android.os.Build;
import android.service.quicksettings.Tile;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Tile.class, minSdk = Build.VERSION_CODES.N)
public final class ShadowTile {

  @Implementation
  protected void updateTile() {}
}
