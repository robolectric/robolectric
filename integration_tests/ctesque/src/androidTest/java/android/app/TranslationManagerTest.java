package android.app;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;

import android.content.Context;
import android.os.Build;
import android.view.translation.TranslationCapability;
import android.view.translation.TranslationManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link TranslationManager}. */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
public class TranslationManagerTest {
  private final Context application = ApplicationProvider.getApplicationContext();
  private boolean hasTranslationManager = false;

  @Before
  public void setUp() {
    // TranslationManager is not always bound into Android device.
    hasTranslationManager = application.getSystemService(TranslationManager.class) != null;
  }

  @Test
  public void translationManager_applicationInstance_isNotSameAsActivityInstance() {
    assumeTrue(hasTranslationManager);
    TranslationManager applicationTranslationManager =
        application.getSystemService(TranslationManager.class);
    assertThat(applicationTranslationManager).isNotNull();
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TranslationManager activityTranslationManager =
                activity.getSystemService(TranslationManager.class);
            assertThat(applicationTranslationManager)
                .isNotSameInstanceAs(activityTranslationManager);
          });
    }
  }

  @Test
  public void translationManager_activityInstance_isSameAsActivityInstance() {
    assumeTrue(hasTranslationManager);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TranslationManager activityTranslationManager =
                activity.getSystemService(TranslationManager.class);
            TranslationManager anotherActivityTranslationManager =
                activity.getSystemService(TranslationManager.class);
            assertThat(anotherActivityTranslationManager)
                .isSameInstanceAs(activityTranslationManager);
          });
    }
  }

  @Test
  public void translationManager_instance_retrievesSameCapabilities() {
    assumeTrue(hasTranslationManager);
    TranslationManager applicationTranslationManager =
        application.getSystemService(TranslationManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            TranslationManager activityTranslationManager =
                activity.getSystemService(TranslationManager.class);

            Set<TranslationCapability> applicationCapabilities =
                applicationTranslationManager.getOnDeviceTranslationCapabilities(1, 2);
            Set<TranslationCapability> activityCapabilities =
                activityTranslationManager.getOnDeviceTranslationCapabilities(1, 2);

            assertThat(activityCapabilities).isEqualTo(applicationCapabilities);
          });
    }
  }
}
