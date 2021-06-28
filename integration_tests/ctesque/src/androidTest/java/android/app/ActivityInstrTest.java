package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.ActivityWithAnotherTheme;
import org.robolectric.testapp.ActivityWithoutTheme;
import org.robolectric.testapp.R;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ActivityInstrTest {

  @Rule
  public ActivityTestRule<ActivityWithAnotherTheme> activityWithAnotherThemeRule =
      new ActivityTestRule<>(ActivityWithAnotherTheme.class, false, false);

  @Rule
  public ActivityTestRule<ActivityWithoutTheme> activityWithoutThemeRule =
      new ActivityTestRule<>(ActivityWithoutTheme.class, false, false);

  @Before
  public void setUp() throws Exception {
    ActivityWithAnotherTheme.setThemeBeforeContentView = null;
  }

  @Test
  public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest()
      throws Exception {
    Activity activity = activityWithAnotherThemeRule.launchActivity(null);
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test
  public void whenExplicitlySetOnActivity_afterSetContentView_activityGetsThemeFromActivityInManifest()
      throws Exception {
    Activity activity = activityWithAnotherThemeRule.launchActivity(null);
    activity.setTheme(R.style.Theme_Robolectric);
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test
  public void whenExplicitlySetOnActivity_beforeSetContentView_activityUsesNewTheme()
      throws Exception {
    ActivityWithAnotherTheme.setThemeBeforeContentView = R.style.Theme_Robolectric;
    Activity activity = activityWithAnotherThemeRule.launchActivity(null);
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

  @Test
  public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest()
      throws Exception {
    Activity activity = activityWithoutThemeRule.launchActivity(null);
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

}
