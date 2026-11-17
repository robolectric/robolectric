package org.robolectric.shadows;

import android.service.quicksettings.Tile;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Tile.class)
public final class ShadowTile {

  @Implementation
  protected void updateTile() {}
}
