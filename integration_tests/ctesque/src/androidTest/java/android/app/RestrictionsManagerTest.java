package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.RestrictionsManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link RestrictionsManager}. */
@RunWith(AndroidJUnit4.class)
public class RestrictionsManagerTest {

  @Test
  public void restrictionsManager_applicationInstance_isNotSameAsActivityInstance() {
    RestrictionsManager applicationRestrictionsManager =
        (RestrictionsManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.RESTRICTIONS_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            RestrictionsManager activityRestrictionsManager =
                (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);

            assertThat(applicationRestrictionsManager)
                .isNotSameInstanceAs(activityRestrictionsManager);
          });
    }
  }

  @Test
  public void restrictionsManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            RestrictionsManager activityRestrictionsManager =
                (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);
            RestrictionsManager anotherActivityRestrictionsManager =
                (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);

            assertThat(anotherActivityRestrictionsManager)
                .isSameInstanceAs(activityRestrictionsManager);
          });
    }
  }

  @Test
  public void restrictionsManager_hasRestrictionsProvider_isConsistentAcrossContexts() {
    RestrictionsManager applicationRestrictionsManager =
        (RestrictionsManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.RESTRICTIONS_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            RestrictionsManager activityRestrictionsManager =
                (RestrictionsManager) activity.getSystemService(Context.RESTRICTIONS_SERVICE);

            boolean applicationHasProvider =
                applicationRestrictionsManager.hasRestrictionsProvider();
            boolean activityHasProvider = activityRestrictionsManager.hasRestrictionsProvider();

            assertThat(activityHasProvider).isEqualTo(applicationHasProvider);
          });
    }
  }
}
