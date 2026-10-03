package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

/**
 * Tests the configuration changes an {@link ActivityScenario}'s activity receives. Note that this
 * test uses Robolectric APIs and thus cannot run on emulators.
 */
@RunWith(AndroidJUnit4.class)
public class ActivityScenarioConfigurationChangeTest {
  @Before
  public void setUp() {
    Application application = ApplicationProvider.getApplicationContext();
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = FontScaleActivity.class.getName();
    activityInfo.packageName = application.getPackageName();
    activityInfo.configChanges = ActivityInfo.CONFIG_FONT_SCALE;
    shadowOf(application.getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  @Test
  public void setFontScale_reportsTheChangeOnce() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      RuntimeEnvironment.setFontScale(2f);

      scenario.onActivity(activity -> assertThat(activity.fontScales).containsExactly(2f));
    }
  }

  @Test
  public void setFontScale_toTheSameScale_reportsNothing() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      RuntimeEnvironment.setFontScale(RuntimeEnvironment.getFontScale());

      scenario.onActivity(activity -> assertThat(activity.fontScales).isEmpty());
    }
  }

  /** Records the font scales it is told about. */
  public static class FontScaleActivity extends Activity {
    final List<Float> fontScales = new ArrayList<>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      fontScales.add(newConfig.fontScale);
    }
  }
}
