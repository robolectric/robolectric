package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.WallpaperManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowWallpaperManagerTest {

  @Test
  public void getInstance_shouldCreateInstance() {
    WallpaperManager manager = WallpaperManager.getInstance(RuntimeEnvironment.application);
    assertThat(manager).isNotNull();
  }

  @Test
  public void sendWallpaperCommand_shouldNotThrowException() {
    WallpaperManager manager = WallpaperManager.getInstance(RuntimeEnvironment.application);
    manager.sendWallpaperCommand(null, null, 0, 0, 0, null);
  }
}
