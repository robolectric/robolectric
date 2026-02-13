package org.robolectric.shadows;


import android.os.Build;
import android.service.quicksettings.Tile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** test for {@link org.robolectric.shadows.ShadowTile}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.N)
public final class ShadowTileTest {

  private Tile tile;
  private ShadowTile shadowTile;

  @Before
  public void setUp() {
    tile = Shadow.newInstanceOf(Tile.class);
    shadowTile = Shadow.extract(tile);
  }

  @Test
  public void updateTile() {
    // this test passes if updateTile() throws no Exception.
    tile.updateTile();
    shadowTile.updateTile();
  }
}
