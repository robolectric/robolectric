package org.robolectric;

import android.service.wallpaper.WallpaperService;

/**
 * An empty implementation of {@link WallpaperService} for testing {@link
 * org.robolectric.shadows.ShadowWallpaperManager}.
 */
public final class TestWallpaperService extends WallpaperService {

  @Override
  public Engine onCreateEngine() {
    return null;
  }
}
