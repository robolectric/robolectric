package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.ActivityWithAnotherTheme;
import org.robolectric.testapp.ActivityWithoutTheme;
import org.robolectric.testapp.R;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ActivityTest {

  @Before
  public void setUp() throws Exception {
    ActivityWithAnotherTheme.setThemeBeforeContentView = null;
  }

  @Test
  public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xffff0000);
          });
    }
  }

  @Test
  public void
      whenExplicitlySetOnActivity_afterSetContentView_activityGetsThemeFromActivityInManifest() {
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            activity.setTheme(R.style.Theme_Robolectric);
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xffff0000);
          });
    }
  }

  @Test
  public void whenExplicitlySetOnActivity_beforeSetContentView_activityUsesNewTheme() {
    ActivityWithAnotherTheme.setThemeBeforeContentView = R.style.Theme_Robolectric;
    try (ActivityScenario<ActivityWithAnotherTheme> scenario =
        ActivityScenario.launch(ActivityWithAnotherTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xff00ff00);
          });
    }
  }

  @Test
  public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() {
    try (ActivityScenario<ActivityWithoutTheme> scenario =
        ActivityScenario.launch(ActivityWithoutTheme.class)) {
      scenario.onActivity(
          activity -> {
            Button theButton = activity.findViewById(R.id.button);
            ColorDrawable background = (ColorDrawable) theButton.getBackground();
            assertThat(background.getColor()).isEqualTo(0xff00ff00);
          });
    }
  }
}
