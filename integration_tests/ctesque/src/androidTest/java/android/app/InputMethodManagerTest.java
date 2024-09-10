package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

/** Compatibility test for {@link InputMethodManager}. */
@RunWith(AndroidJUnit4.class)
public class InputMethodManagerTest {
  @Test
  public void inputMethodManager_applicationInstance_isSameAsActivityInstance() {
    InputMethodManager applicationInputMethodManager =
        (InputMethodManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            InputMethodManager activityInputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assertThat(applicationInputMethodManager).isSameInstanceAs(activityInputMethodManager);
          });
    }
  }

  @Test
  public void inputMethodManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            InputMethodManager activityInputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager anotherActivityInputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            assertThat(anotherActivityInputMethodManager)
                .isSameInstanceAs(activityInputMethodManager);
          });
    }
  }

  @Test
  public void inputMethodManager_retrievesSameIsAcceptingText() {
    InputMethodManager applicationInputMethodManager =
        (InputMethodManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            InputMethodManager activityInputMethodManager =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            boolean applicationIsAcceptingText = applicationInputMethodManager.isAcceptingText();
            boolean activityIsAcceptingText = activityInputMethodManager.isAcceptingText();

            assertThat(activityIsAcceptingText).isEqualTo(applicationIsAcceptingText);
          });
    }
  }
}
