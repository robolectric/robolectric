package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link SearchManager}. */
@RunWith(AndroidJUnit4.class)
public class SearchManagerTest {

  @Test
  public void searchManager_applicationInstance_isNotSameAsActivityInstance() {
    SearchManager applicationSearchManager =
        (SearchManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SEARCH_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SearchManager activitySearchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
            assertThat(applicationSearchManager).isNotSameInstanceAs(activitySearchManager);
          });
    }
  }

  @Test
  public void searchManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SearchManager activitySearchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
            SearchManager anotherActivitySearchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
            assertThat(anotherActivitySearchManager).isSameInstanceAs(activitySearchManager);
          });
    }
  }

  @Test
  public void searchManager_globalSearchActivity_retrievesSameValues() {
    SearchManager applicationSearchManager =
        (SearchManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SEARCH_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SearchManager activitySearchManager =
                (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);

            ComponentName applicationGlobalSearchActivity =
                applicationSearchManager.getGlobalSearchActivity();
            ComponentName activityGlobalSearchActivity =
                activitySearchManager.getGlobalSearchActivity();

            assertThat(applicationGlobalSearchActivity).isEqualTo(activityGlobalSearchActivity);
          });
    }
  }
}
