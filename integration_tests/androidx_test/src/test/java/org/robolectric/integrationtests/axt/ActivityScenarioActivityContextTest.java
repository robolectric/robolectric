package org.robolectric.integrationtests.axt;

import static android.os.Build.VERSION_CODES.O;
import static androidx.test.core.app.ActivityScenario.launch;
import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Tests {@link androidx.test.core.app.ActivityScenario} with realistic Activity Contexts. Note that
 * this test uses Robolectric APIs and thus cannot run on emulators.
 */
@RunWith(AndroidJUnit4.class)
public class ActivityScenarioActivityContextTest {

  private static String originalProperty;

  @BeforeClass
  public static void beforeClass() {
    originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
  }

  @AfterClass
  public static void afterClass() {
    System.setProperty("robolectric.createActivityContexts", originalProperty);
  }

  /**
   * This should be run on all SDK levels to ensure that setting qualifiers does not cause an
   * infinite loop when using activity contexts.
   */
  @Test
  public void setQualifiers_doesNotStackOverflow() {
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      scenario.onActivity(
          activity -> {
            // Check that RuntimeEnvironment.setQualifiers does not cause an infinite loop
            RuntimeEnvironment.setQualifiers("+land");
          });
    }
  }

  @Test
  @Config(minSdk = O)
  public void baseContext_notApplicationContext() {
    final Application applicationContext = RuntimeEnvironment.getApplication();
    try (ActivityScenario<EspressoActivity> scenario = launch(EspressoActivity.class)) {
      scenario.onActivity(
          activity ->
              assertThat(activity.getBaseContext())
                  .isNotEqualTo(applicationContext.getBaseContext()));
    }
  }
}
