package org.robolectric.integrationtests.axt;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Tests the configuration changes an {@link ActivityScenario}'s activity receives. Note that this
 * test uses Robolectric APIs and thus cannot run on emulators.
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.ALL_SDKS)
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

  @Test
  @Config(minSdk = VERSION_CODES.O, qualifiers = "port")
  public void configurationChange_whenStopped_recreatesWhenResumed() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      AtomicReference<FontScaleActivity> initial = new AtomicReference<>();
      scenario.onActivity(initial::set);
      scenario.moveToState(Lifecycle.State.CREATED);

      RuntimeEnvironment.setQualifiers("+land");

      scenario.onActivity(activity -> assertThat(activity).isSameInstanceAs(initial.get()));
      scenario.moveToState(Lifecycle.State.RESUMED);
      scenario.onActivity(activity -> {
        assertThat(activity).isNotSameInstanceAs(initial.get());
        assertThat(activity.getResources().getConfiguration().orientation)
            .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
        assertThat(activity.getWindow().getDecorView().isAttachedToWindow()).isTrue();
      });
    }
  }

  @Test
  @Config(maxSdk = VERSION_CODES.N_MR1, qualifiers = "port")
  public void configurationChange_whenStopped_showsRecreatedActivityOnResume() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      AtomicReference<FontScaleActivity> initial = new AtomicReference<>();
      scenario.onActivity(initial::set);
      scenario.moveToState(Lifecycle.State.CREATED);

      RuntimeEnvironment.setQualifiers("+land");

      scenario.moveToState(Lifecycle.State.RESUMED);
      scenario.onActivity(activity -> {
        assertThat(activity).isNotSameInstanceAs(initial.get());
        assertThat(activity.getWindow().getDecorView().isAttachedToWindow()).isTrue();
      });
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.O)
  public void configurationChange_whenStopped_deliversLatestHandledChangeOnResume() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      AtomicReference<FontScaleActivity> initial = new AtomicReference<>();
      scenario.onActivity(initial::set);
      scenario.moveToState(Lifecycle.State.CREATED);

      RuntimeEnvironment.setFontScale(1.5f);
      RuntimeEnvironment.setFontScale(2f);

      scenario.onActivity(activity -> assertThat(activity.fontScales).isEmpty());
      scenario.moveToState(Lifecycle.State.RESUMED);
      scenario.onActivity(activity -> {
        assertThat(activity).isSameInstanceAs(initial.get());
        assertThat(activity.fontScales).containsExactly(2f);
      });
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.O, qualifiers = "port")
  public void configurationChange_whenStoppedAndReverted_doesNotRecreate() {
    try (ActivityScenario<FontScaleActivity> scenario =
        ActivityScenario.launch(FontScaleActivity.class)) {
      AtomicReference<FontScaleActivity> initial = new AtomicReference<>();
      scenario.onActivity(initial::set);
      scenario.moveToState(Lifecycle.State.CREATED);

      RuntimeEnvironment.setQualifiers("+land");
      RuntimeEnvironment.setQualifiers("+port");

      scenario.moveToState(Lifecycle.State.RESUMED);
      scenario.onActivity(activity -> assertThat(activity).isSameInstanceAs(initial.get()));
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
