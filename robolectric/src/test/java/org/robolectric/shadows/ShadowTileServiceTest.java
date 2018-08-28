package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** test for {@link org.robolectric.shadows.ShadowTileService}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.N)
public final class ShadowTileServiceTest {

  private MyTileService tileService;

  @Before
  public void setUp() {
    tileService = Shadow.newInstanceOf(MyTileService.class);
  }

  @Test
  public void getTile() {
    Tile tile = tileService.getQsTile();
    assertThat(tile).isNotNull();
  }

  /** A subclass of {@link TileService} for testing,
   *  to mimic the way {@link TileService} is used in production.*/
  static class MyTileService extends TileService {}
}
