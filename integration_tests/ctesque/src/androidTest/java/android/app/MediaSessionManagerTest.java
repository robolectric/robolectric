package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.session.MediaSessionManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link MediaSessionManager}. */
@RunWith(AndroidJUnit4.class)
public class MediaSessionManagerTest {

  @Test
  public void mediaSessionManager_applicationInstance_isNotSameAsActivityInstance() {
    MediaSessionManager applicationMediaSessionManager =
        (MediaSessionManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_SESSION_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaSessionManager activityMediaSessionManager =
                (MediaSessionManager) activity.getSystemService(Context.MEDIA_SESSION_SERVICE);
            assertThat(applicationMediaSessionManager)
                .isNotSameInstanceAs(activityMediaSessionManager);
          });
    }
  }

  @Test
  public void mediaSessionManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaSessionManager activityMediaSessionManager =
                (MediaSessionManager) activity.getSystemService(Context.MEDIA_SESSION_SERVICE);
            MediaSessionManager anotherActivityMediaSessionManager =
                (MediaSessionManager) activity.getSystemService(Context.MEDIA_SESSION_SERVICE);
            assertThat(anotherActivityMediaSessionManager)
                .isSameInstanceAs(activityMediaSessionManager);
          });
    }
  }

  @Test
  public void mediaSessionManager_isTrustedForMediaControl_retrievesCorrectTrustStatus() {
    MediaSessionManager applicationMediaSessionManager =
        (MediaSessionManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_SESSION_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaSessionManager activityMediaSessionManager =
                (MediaSessionManager) activity.getSystemService(Context.MEDIA_SESSION_SERVICE);

            MediaSessionManager.RemoteUserInfo userInfo =
                new MediaSessionManager.RemoteUserInfo("com.example.package", 1234, 5678);

            boolean applicationIsTrusted =
                applicationMediaSessionManager.isTrustedForMediaControl(userInfo);
            boolean activityIsTrusted =
                activityMediaSessionManager.isTrustedForMediaControl(userInfo);

            assertThat(activityIsTrusted).isEqualTo(applicationIsTrusted);
          });
    }
  }
}
