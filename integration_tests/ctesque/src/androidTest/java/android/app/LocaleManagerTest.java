package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Build;
import android.os.LocaleList;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link LocaleManager}. */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.TIRAMISU)
public class LocaleManagerTest {

  @Test
  public void localeManager_applicationInstance_isNotSameAsActivityInstance() {
    LocaleManager applicationLocaleManager =
        (LocaleManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.LOCALE_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LocaleManager activityLocaleManager =
                (LocaleManager) activity.getSystemService(Context.LOCALE_SERVICE);
            assertThat(applicationLocaleManager).isNotSameInstanceAs(activityLocaleManager);
          });
    }
  }

  @Test
  public void localeManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LocaleManager activityLocaleManager =
                (LocaleManager) activity.getSystemService(Context.LOCALE_SERVICE);
            LocaleManager anotherActivityLocaleManager =
                (LocaleManager) activity.getSystemService(Context.LOCALE_SERVICE);
            assertThat(anotherActivityLocaleManager).isSameInstanceAs(activityLocaleManager);
          });
    }
  }

  @Test
  public void localeManager_instance_retrievesSameApplicationLocales() {
    LocaleManager applicationLocaleManager =
        (LocaleManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.LOCALE_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LocaleManager activityLocaleManager =
                (LocaleManager) activity.getSystemService(Context.LOCALE_SERVICE);

            LocaleList applicationLocales = applicationLocaleManager.getApplicationLocales();
            LocaleList activityLocales = activityLocaleManager.getApplicationLocales();

            assertThat(activityLocales).isEqualTo(applicationLocales);
          });
    }
  }
}
