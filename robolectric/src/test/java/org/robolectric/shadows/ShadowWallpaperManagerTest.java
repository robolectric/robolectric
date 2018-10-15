package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.WallpaperManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AndroidJUnit4.class)
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
