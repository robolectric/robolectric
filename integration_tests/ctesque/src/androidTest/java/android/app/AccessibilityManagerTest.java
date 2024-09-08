package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link AccessibilityManager}. */
@RunWith(AndroidJUnit4.class)
public class AccessibilityManagerTest {

  @Test
  public void accessibilityManager_applicationInstance_isSameAsActivityInstance() {
    AccessibilityManager applicationAccessibilityManager =
        (AccessibilityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccessibilityManager activityAccessibilityManager =
                (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);
            assertThat(applicationAccessibilityManager)
                .isSameInstanceAs(activityAccessibilityManager);
          });
    }
  }

  @Test
  public void accessibilityManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccessibilityManager activityAccessibilityManager =
                (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);
            AccessibilityManager anotherActivityAccessibilityManager =
                (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);
            assertThat(anotherActivityAccessibilityManager)
                .isSameInstanceAs(activityAccessibilityManager);
          });
    }
  }

  @Test
  public void accessibilityManager_instance_hasSameAccessibilityServices() {
    AccessibilityManager applicationAccessibilityManager =
        (AccessibilityManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.ACCESSIBILITY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccessibilityManager activityAccessibilityManager =
                (AccessibilityManager) activity.getSystemService(Context.ACCESSIBILITY_SERVICE);

            List<AccessibilityServiceInfo> applicationServices =
                applicationAccessibilityManager.getInstalledAccessibilityServiceList();
            List<AccessibilityServiceInfo> activityServices =
                activityAccessibilityManager.getInstalledAccessibilityServiceList();

            assertThat(activityServices).isEqualTo(applicationServices);
          });
    }
  }
}
