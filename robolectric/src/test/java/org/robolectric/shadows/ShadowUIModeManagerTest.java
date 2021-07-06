package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** */
@RunWith(AndroidJUnit4.class)
@Config(sdk = VERSION_CODES.LOLLIPOP, shadows = ShadowUIModeManager.class)
public class ShadowUIModeManagerTest {
  private UiModeManager uiModeManager;

  @Before
  public void setUp() {
    uiModeManager =
        (UiModeManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
  }

  @Test
  @Config(minSdk = M)
  public void testModeSwitch() {
    assertThat(uiModeManager.getCurrentModeType()).isEqualTo(Configuration.UI_MODE_TYPE_UNDEFINED);
    assertThat(shadowOf(uiModeManager).lastFlags).isEqualTo(0);

    uiModeManager.enableCarMode(1);
    assertThat(uiModeManager.getCurrentModeType()).isEqualTo(Configuration.UI_MODE_TYPE_CAR);
    assertThat(shadowOf(uiModeManager).lastFlags).isEqualTo(1);

    uiModeManager.disableCarMode(2);
    assertThat(uiModeManager.getCurrentModeType()).isEqualTo(Configuration.UI_MODE_TYPE_NORMAL);
    assertThat(shadowOf(uiModeManager).lastFlags).isEqualTo(2);
  }

  @Test
  @Config(minSdk = R)
  public void testCarModePriority() {
    int priority = 9;
    int flags = 1;
    uiModeManager.enableCarMode(priority, flags);
    assertThat(uiModeManager.getCurrentModeType()).isEqualTo(Configuration.UI_MODE_TYPE_CAR);
    assertThat(shadowOf(uiModeManager).lastCarModePriority).isEqualTo(priority);
    assertThat(shadowOf(uiModeManager).lastFlags).isEqualTo(flags);
  }

  private static final int INVALID_NIGHT_MODE = -4242;

  @Test
  public void testNightMode() {
    assertThat(uiModeManager.getNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_AUTO);

    uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_YES);
    assertThat(uiModeManager.getNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_YES);

    uiModeManager.setNightMode(UiModeManager.MODE_NIGHT_NO);
    assertThat(uiModeManager.getNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_NO);

    uiModeManager.setNightMode(INVALID_NIGHT_MODE);
    assertThat(uiModeManager.getNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_AUTO);
  }

  @Test
  @Config(minSdk = S)
  public void testApplicationNightMode() {
    assertThat(uiModeManager.getApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_AUTO);

    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_YES);
    assertThat(uiModeManager.getApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_YES);

    uiModeManager.setApplicationNightMode(UiModeManager.MODE_NIGHT_NO);
    assertThat(uiModeManager.getApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_NO);

    uiModeManager.setApplicationNightMode(INVALID_NIGHT_MODE);
    assertThat(uiModeManager.getApplicationNightMode()).isEqualTo(UiModeManager.MODE_NIGHT_AUTO);
  }
}
