package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.UserManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link UserManager}. */
@RunWith(AndroidJUnit4.class)
public class UserManagerTest {

  @Test
  public void userManager_activityInstance_isSameAsAnotherActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UserManager activityUserManager =
                (UserManager) activity.getSystemService(Context.USER_SERVICE);
            UserManager anotherActivityUserManager =
                (UserManager) activity.getSystemService(Context.USER_SERVICE);
            assertThat(anotherActivityUserManager).isSameInstanceAs(activityUserManager);
          });
    }
  }

  @Test
  public void userManager_applicationInstance_isNotSameAsActivityInstance() {
    UserManager applicationUserManager =
        (UserManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USER_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UserManager activityUserManager =
                (UserManager) activity.getSystemService(Context.USER_SERVICE);
            assertThat(applicationUserManager).isNotSameInstanceAs(activityUserManager);
          });
    }
  }

  @Test
  public void userManager_isUserAGoat_consistentAcrossContexts() {
    UserManager applicationUserManager =
        (UserManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USER_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            UserManager activityUserManager =
                (UserManager) activity.getSystemService(Context.USER_SERVICE);

            boolean isUserAGoatApplication = applicationUserManager.isUserAGoat();
            boolean isUserAGoatActivity = activityUserManager.isUserAGoat();

            assertThat(isUserAGoatApplication).isEqualTo(isUserAGoatActivity);
          });
    }
  }
}
