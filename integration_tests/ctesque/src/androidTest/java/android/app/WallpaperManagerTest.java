package android.app;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link WallpaperManager}. */
@RunWith(AndroidJUnit4.class)
public class WallpaperManagerTest {
  private WallpaperManager wallpaperManager;
  private boolean isWallpaperSupported = false;

  @Before
  public void setUp() {
    // Test code can't access com.android.internal config value to check whether the current running
    // Android
    // supports WallpaperManagerService, and it uses returned WallpaperManager's flag to check it.
    Object manager =
        ApplicationProvider.getApplicationContext().getSystemService(Context.WALLPAPER_SERVICE);
    if (manager != null) {
      wallpaperManager = (WallpaperManager) manager;
    }

    isWallpaperSupported = wallpaperManager != null && wallpaperManager.isWallpaperSupported();
  }

  @Test
  public void wallpaperManager_applicationInstance_matchesExpectedBehavior() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WallpaperManager activityWallpaperManager =
                (WallpaperManager) activity.getSystemService(Context.WALLPAPER_SERVICE);
            // If WallpaperManagerService is not supported by the device, it returns a special
            // WindowManager implementation called DisabledWallpaperManager, and it uses
            // singleton for all Context instances.
            if (isWallpaperSupported) {
              assertThat(wallpaperManager).isNotSameInstanceAs(activityWallpaperManager);
            } else {
              assertThat(wallpaperManager).isSameInstanceAs(activityWallpaperManager);
            }
          });
    }
  }

  @Test
  public void wallpaperManager_activityInstance_isSameAsActivityInstance() {
    assumeTrue(isWallpaperSupported);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WallpaperManager activityWallpaperManager =
                (WallpaperManager) activity.getSystemService(Context.WALLPAPER_SERVICE);
            WallpaperManager anotherActivityWallpaperManager =
                (WallpaperManager) activity.getSystemService(Context.WALLPAPER_SERVICE);
            assertThat(anotherActivityWallpaperManager).isSameInstanceAs(activityWallpaperManager);
          });
    }
  }

  @Test
  public void wallpaperManager_instance_retrievesSameWallpaper() {
    assumeTrue(isWallpaperSupported);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            WallpaperManager activityWallpaperManager =
                (WallpaperManager) activity.getSystemService(Context.WALLPAPER_SERVICE);

            WallpaperInfo applicationWallpaper = wallpaperManager.getWallpaperInfo();
            WallpaperInfo activityWallpaper = activityWallpaperManager.getWallpaperInfo();

            assertThat(activityWallpaper).isEqualTo(applicationWallpaper);
          });
    }
  }
}
