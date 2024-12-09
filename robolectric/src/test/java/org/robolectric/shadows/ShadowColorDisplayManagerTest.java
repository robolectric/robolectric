package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadow.api.Shadow.extract;

import android.app.Activity;
import android.hardware.display.ColorDisplayManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Optional;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Tests for ShadowColorDisplayManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // getAppSaturationLevel_afterReset* depends on order
public class ShadowColorDisplayManagerTest {

  private static final String PACKAGE_NAME = "test_package_name";

  // Must be optional to avoid ClassNotFoundException
  Optional<ColorDisplayManager> instance;

  @Before
  public void setUp() throws Exception {
    instance =
        Optional.of(
            ApplicationProvider.getApplicationContext()
                .getSystemService(ColorDisplayManager.class));
  }

  @Test
  public void getSaturationLevel_defaultValue_shouldReturnHundred() {
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(100);
  }

  @Test
  public void getSaturationLevel_setToZero_shouldReturnZero() {
    instance.get().setSaturationLevel(0);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(0);
  }

  @Test
  public void getSaturationLevel_setToHalf_shouldReturnHalf() {
    instance.get().setSaturationLevel(50);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(50);
  }

  @Test
  public void getSaturationLevel_setToHundred_shouldReturnHundred() {
    instance.get().setSaturationLevel(0);
    instance.get().setSaturationLevel(100);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(100);
  }

  @Test
  public void getSaturationLevel_setToZeroViaShadow_shouldReturnZero() {
    getShadowColorDisplayManager().setSaturationLevel(0);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(0);
  }

  @Test
  public void getSaturationLevel_setToHalfViaShadow_shouldReturnHalf() {
    getShadowColorDisplayManager().setSaturationLevel(50);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(50);
  }

  @Test
  public void getSaturationLevel_setToHundredViaShadow_shouldReturnHundred() {
    getShadowColorDisplayManager().setSaturationLevel(0);
    getShadowColorDisplayManager().setSaturationLevel(100);
    assertThat(getShadowColorDisplayManager().getSaturationLevel()).isEqualTo(100);
  }

  @Test
  public void getAppSaturationLevel_defaultValue_shouldReturnHundred() {
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(100);
  }

  @Test
  public void getAppSaturationLevel_setToZero_shouldReturnZero() {
    instance.get().setAppSaturationLevel(PACKAGE_NAME, 0);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(0);
  }

  @Test
  public void getAppSaturationLevel_setToHalf_shouldReturnHalf() {
    instance.get().setAppSaturationLevel(PACKAGE_NAME, 50);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(50);
  }

  @Test
  public void getAppSaturationLevel_setToHundred_shouldReturnHundred() {
    instance.get().setAppSaturationLevel(PACKAGE_NAME, 0);
    instance.get().setAppSaturationLevel(PACKAGE_NAME, 100);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(100);
  }

  @Test
  public void getAppSaturationLevel_setToZeroViaShadow_shouldReturnZero() {
    getShadowColorDisplayManager().setAppSaturationLevel(PACKAGE_NAME, 0);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(0);
  }

  @Test
  public void getAppSaturationLevel_setToHalfViaShadow_shouldReturnHalf() {
    getShadowColorDisplayManager().setAppSaturationLevel(PACKAGE_NAME, 50);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(50);
  }

  @Test
  public void getAppSaturationLevel_setToHundredViaShadow_shouldReturnHundred() {
    getShadowColorDisplayManager().setAppSaturationLevel(PACKAGE_NAME, 0);
    getShadowColorDisplayManager().setAppSaturationLevel(PACKAGE_NAME, 100);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(100);
  }

  @Test
  public void getTransformCapabilities_defaultNone_shouldReturnNoCapabilities() {
    assertThat(getShadowColorDisplayManager().getTransformCapabilities()).isEqualTo(0x0);
  }

  @Test
  public void getTransformCapabilities_setToFull_shouldReturnFullCapabilities() {
    getShadowColorDisplayManager().setTransformCapabilities(0x4);
    assertThat(getShadowColorDisplayManager().getTransformCapabilities()).isEqualTo(0x4);
  }

  @Test
  public void getTransformCapabilities_setToZero_shouldReturnNoCapabilities() {
    getShadowColorDisplayManager().setTransformCapabilities(0x0);
    assertThat(getShadowColorDisplayManager().getTransformCapabilities()).isEqualTo(0x0);
  }

  @Test
  public void getAppSaturationLevel_afterReset_shouldBeDefault1() {
    instance.get().setAppSaturationLevel(PACKAGE_NAME, 50);
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(50);
  }

  @Test
  public void getAppSaturationLevel_afterReset_shouldBeDefault2() {
    // A reset should have occurred
    assertThat(getShadowColorDisplayManager().getAppSaturationLevel(PACKAGE_NAME)).isEqualTo(100);
  }

  private ShadowColorDisplayManager getShadowColorDisplayManager() {
    return extract(instance.get());
  }

  @Test
  public void colorDisplayManager_activityContextEnabled_differentInstancesRetrieveSettings() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      ColorDisplayManager appColorDisplayManager =
          ApplicationProvider.getApplicationContext().getSystemService(ColorDisplayManager.class);
      activity = Robolectric.setupActivity(Activity.class);
      ColorDisplayManager activityColorDisplayManager =
          activity.getSystemService(ColorDisplayManager.class);

      assertThat(appColorDisplayManager).isNotSameInstanceAs(activityColorDisplayManager);

      boolean appNightDisplayActivated =
          ((ShadowColorDisplayManager) extract(appColorDisplayManager)).isNightDisplayActivated();
      boolean activityNightDisplayActivated =
          ((ShadowColorDisplayManager) extract(activityColorDisplayManager))
              .isNightDisplayActivated();

      assertThat(activityNightDisplayActivated).isEqualTo(appNightDisplayActivated);

    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
