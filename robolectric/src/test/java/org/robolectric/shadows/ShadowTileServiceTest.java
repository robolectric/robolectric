package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link org.robolectric.shadows.ShadowTileService}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.N)
public final class ShadowTileServiceTest {

  private MyTileService tileService;

  @Before
  public void setUp() {
    tileService = new MyTileService();
  }

  @Test
  public void getTile() {
    Tile tile = tileService.getQsTile();
    assertThat(tile).isNotNull();
  }

  /**
   * A subclass of {@link TileService} for testing, To mimic the way {@link TileService} is used in
   * production.
   */
  static class MyTileService extends TileService {}
}
