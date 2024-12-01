package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.security.FileIntegrityManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public final class ShadowFileIntegrityManagerTest {

  private FileIntegrityManager fileIntegrityManager;

  @Before
  public void setUp() {
    fileIntegrityManager =
        ApplicationProvider.getApplicationContext().getSystemService(FileIntegrityManager.class);
  }

  @Test
  public void isApkVeritySupported_returnsTrue() {
    assertThat(fileIntegrityManager.isApkVeritySupported()).isTrue();
  }

  @Test
  public void isApkVeritySupported_setFalse_returnsFalse() {
    ((ShadowFileIntegrityManager) Shadow.extract(fileIntegrityManager))
        .setIsApkVeritySupported(false);

    assertThat(fileIntegrityManager.isApkVeritySupported()).isFalse();
  }

  @Test
  public void fileIntegrityManager_activityContextEnabled_retrievesSameValues() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      FileIntegrityManager applicationFileIntegrityManager =
          (FileIntegrityManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.FILE_INTEGRITY_SERVICE);

      Activity activity = controller.get();
      FileIntegrityManager activityFileIntegrityManager =
          (FileIntegrityManager) activity.getSystemService(Context.FILE_INTEGRITY_SERVICE);

      assertThat(applicationFileIntegrityManager).isNotSameInstanceAs(activityFileIntegrityManager);

      boolean applicationApkVeritySupported =
          Objects.requireNonNull(applicationFileIntegrityManager).isApkVeritySupported();
      boolean activityApkVeritySupported =
          Objects.requireNonNull(activityFileIntegrityManager).isApkVeritySupported();

      assertThat(activityApkVeritySupported).isEqualTo(applicationApkVeritySupported);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
