package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Build;
import android.security.FileIntegrityManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link FileIntegrityManager}. */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.TIRAMISU)
public class FileIntegrityManagerTest {

  @Test
  public void fileIntegrityManager_applicationInstance_isNotSameAsActivityInstance() {
    FileIntegrityManager applicationFileIntegrityManager =
        (FileIntegrityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FILE_INTEGRITY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FileIntegrityManager activityFileIntegrityManager =
                (FileIntegrityManager) activity.getSystemService(Context.FILE_INTEGRITY_SERVICE);

            assertThat(applicationFileIntegrityManager)
                .isNotSameInstanceAs(activityFileIntegrityManager);
          });
    }
  }

  @Test
  public void fileIntegrityManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FileIntegrityManager activityFileIntegrityManager =
                (FileIntegrityManager) activity.getSystemService(Context.FILE_INTEGRITY_SERVICE);
            FileIntegrityManager anotherActivityFileIntegrityManager =
                (FileIntegrityManager) activity.getSystemService(Context.FILE_INTEGRITY_SERVICE);
            assertThat(anotherActivityFileIntegrityManager)
                .isSameInstanceAs(activityFileIntegrityManager);
          });
    }
  }

  @Test
  public void fileIntegrityManager_instance_retrievesSameValues() {
    FileIntegrityManager applicationFileIntegrityManager =
        (FileIntegrityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FILE_INTEGRITY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FileIntegrityManager activityFileIntegrityManager =
                (FileIntegrityManager) activity.getSystemService(Context.FILE_INTEGRITY_SERVICE);

            boolean applicationApkVeritySupported =
                applicationFileIntegrityManager.isApkVeritySupported();
            boolean activityApkVeritySupported =
                activityFileIntegrityManager.isApkVeritySupported();

            assertThat(activityApkVeritySupported).isEqualTo(applicationApkVeritySupported);
          });
    }
  }
}
