package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;

import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowUiAutomation}. */
@Config(minSdk = JELLY_BEAN_MR2)
@RunWith(AndroidJUnit4.class)
public class ShadowUiAutomationTest {
  @Config(sdk = JELLY_BEAN_MR1)
  @Test
  public void setAnimationScale_zero() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(0);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(0);
  }

  @Config(sdk = JELLY_BEAN_MR1)
  @Test
  public void setAnimationScale_one() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(1);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(1);
  }

  @Config(sdk = JELLY_BEAN)
  @Test
  public void setAnimationScale_zero_jellyBean() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(0);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.System.getFloat(cr, Settings.System.ANIMATOR_DURATION_SCALE)).isEqualTo(0);
    assertThat(Settings.System.getFloat(cr, Settings.System.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(0);
    assertThat(Settings.System.getFloat(cr, Settings.System.WINDOW_ANIMATION_SCALE)).isEqualTo(0);
  }

  @Config(sdk = JELLY_BEAN)
  @Test
  public void setAnimationScale_one_jellyBean() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(1);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.System.getFloat(cr, Settings.System.ANIMATOR_DURATION_SCALE)).isEqualTo(1);
    assertThat(Settings.System.getFloat(cr, Settings.System.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(1);
    assertThat(Settings.System.getFloat(cr, Settings.System.WINDOW_ANIMATION_SCALE)).isEqualTo(1);
  }

  @Test
  public void setRotation_freeze90_rotatesToLandscape() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_90);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

  @Test
  public void setRotation_freeze180_rotatesToPortrait() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_180);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }

  @Test
  public void setRotation_freezeCurrent_doesNothing() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_CURRENT);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_0);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }
}
