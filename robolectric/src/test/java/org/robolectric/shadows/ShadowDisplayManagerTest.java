package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowDisplayManagerTest.HideFromJB.getGlobal;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowDisplayManagerTest {

  private DisplayManager instance;

  @Before
  public void setUp() throws Exception {
    instance =
        (DisplayManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
  }

  @Test
  @Config(maxSdk = JELLY_BEAN)
  public void notSupportedInJellyBean() {
    try {
      ShadowDisplayManager.removeDisplay(0);
      fail("Expected Exception thrown");
    } catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageThat().contains("displays not supported in Jelly Bean");
    }
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getDisplayInfo_shouldReturnCopy() {
    DisplayInfo displayInfo = getGlobal().getDisplayInfo(Display.DEFAULT_DISPLAY);
    int origAppWidth = displayInfo.appWidth;
    displayInfo.appWidth++;
    assertThat(getGlobal().getDisplayInfo(Display.DEFAULT_DISPLAY).appWidth)
        .isEqualTo(origAppWidth);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void forNonexistentDisplay_getDisplayInfo_shouldReturnNull() {
    assertThat(getGlobal().getDisplayInfo(3)).isEqualTo(null);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void forNonexistentDisplay_changeDisplay_shouldThrow() {
    try {
      ShadowDisplayManager.changeDisplay(3, "");
      fail("Expected Exception thrown");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().contains("no display 3");
    }
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void forNonexistentDisplay_removeDisplay_shouldThrow() {
    try {
      ShadowDisplayManager.removeDisplay(3);
      fail("Expected Exception thrown");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().contains("no display 3");
    }
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void addDisplay() {
    int displayId = ShadowDisplayManager.addDisplay("w100dp-h200dp");
    assertThat(displayId).isGreaterThan(0);

    DisplayInfo di = getGlobal().getDisplayInfo(displayId);
    assertThat(di.appWidth).isEqualTo(100);
    assertThat(di.appHeight).isEqualTo(200);

    Display display = instance.getDisplay(displayId);
    assertThat(display.getDisplayId()).isEqualTo(displayId);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void addDisplay_shouldNotifyListeners() {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), null);
    int displayId = ShadowDisplayManager.addDisplay("w100dp-h200dp");
    assertThat(events).containsExactly("Added " + displayId);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void changeDisplay_shouldUpdateSmallestAndLargestNominalWidthAndHeight() {
    Point smallest = new Point();
    Point largest = new Point();

    ShadowDisplay.getDefaultDisplay().getCurrentSizeRange(smallest, largest);
    assertThat(smallest).isEqualTo(new Point(320, 320));
    assertThat(largest).isEqualTo(new Point(470, 470));

    Display display = ShadowDisplay.getDefaultDisplay();
    ShadowDisplay shadowDisplay = Shadow.extract(display);
    shadowDisplay.setWidth(display.getWidth() - 10);
    shadowDisplay.setHeight(display.getHeight() - 10);

    ShadowDisplay.getDefaultDisplay().getCurrentSizeRange(smallest, largest);
    assertThat(smallest).isEqualTo(new Point(310, 310));
    assertThat(largest).isEqualTo(new Point(460, 460));
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void withQualifiers_changeDisplay_shouldUpdateSmallestAndLargestNominalWidthAndHeight() {
    Point smallest = new Point();
    Point largest = new Point();

    Display display = ShadowDisplay.getDefaultDisplay();
    display.getCurrentSizeRange(smallest, largest);
    assertThat(smallest).isEqualTo(new Point(320, 320));
    assertThat(largest).isEqualTo(new Point(470, 470));

    ShadowDisplayManager.changeDisplay(display.getDisplayId(), "w310dp-h460dp");

    display.getCurrentSizeRange(smallest, largest);
    assertThat(smallest).isEqualTo(new Point(310, 310));
    assertThat(largest).isEqualTo(new Point(460, 460));
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void changeAndRemoveDisplay_shouldNotifyListeners() {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), null);
    int displayId = ShadowDisplayManager.addDisplay("w100dp-h200dp");

    ShadowDisplayManager.changeDisplay(displayId, "w300dp-h400dp");

    Display display = getGlobal().getRealDisplay(displayId);
    assertThat(display.getWidth()).isEqualTo(300);
    assertThat(display.getHeight()).isEqualTo(400);
    assertThat(display.getOrientation()).isEqualTo(Surface.ROTATION_0);

    ShadowDisplayManager.removeDisplay(displayId);

    assertThat(events).containsExactly(
        "Added " + displayId,
        "Changed " + displayId,
        "Removed " + displayId);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void changeDisplay_shouldAllowPartialChanges() {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), null);
    int displayId = ShadowDisplayManager.addDisplay("w100dp-h200dp");

    ShadowDisplayManager.changeDisplay(displayId, "+h201dp-land");

    Display display = getGlobal().getRealDisplay(displayId);
    assertThat(display.getWidth()).isEqualTo(201);
    assertThat(display.getHeight()).isEqualTo(100);
    assertThat(display.getOrientation()).isEqualTo(Surface.ROTATION_90);

    assertThat(events).containsExactly(
        "Added " + displayId,
        "Changed " + displayId);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void modeBuilder_setsModeParameters() {
    int modeId = 5;
    int width = 500;
    int height = 1000;
    float refreshRate = 60.f;
    Display.Mode mode =
        ShadowDisplayManager.ModeBuilder.modeBuilder(modeId)
            .setWidth(width)
            .setHeight(height)
            .setRefreshRate(refreshRate)
            .build();
    assertThat(mode.getPhysicalWidth()).isEqualTo(width);
    assertThat(mode.getPhysicalHeight()).isEqualTo(height);
    assertThat(mode.getModeId()).isEqualTo(modeId);
    assertThat(mode.getRefreshRate()).isEqualTo(refreshRate);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void setSupportedModes_addsOneDisplayMode() {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), /* handler= */ null);
    int displayId = ShadowDisplayManager.addDisplay(/* qualifiersStr= */ "w100dp-h200dp");

    Display.Mode mode =
        ShadowDisplayManager.ModeBuilder.modeBuilder(0)
            .setWidth(500)
            .setHeight(500)
            .setRefreshRate(60)
            .build();
    ShadowDisplayManager.setSupportedModes(displayId, mode);

    Display.Mode[] modes = getGlobal().getRealDisplay(displayId).getSupportedModes();
    assertThat(modes).hasLength(1);
    assertThat(modes).asList().containsExactly(mode);

    assertThat(events).containsExactly("Added " + displayId, "Changed " + displayId);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void setSupportedModes_addsMultipleDisplayModes() {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), /* handler= */ null);
    int displayId = ShadowDisplayManager.addDisplay(/* qualifiersStr= */ "w100dp-h200dp");

    Display.Mode[] modesToSet =
        new Display.Mode[] {
          ShadowDisplayManager.ModeBuilder.modeBuilder(0)
              .setWidth(500)
              .setHeight(500)
              .setRefreshRate(60)
              .build(),
          ShadowDisplayManager.ModeBuilder.modeBuilder(0)
              .setWidth(1000)
              .setHeight(1500)
              .setRefreshRate(120)
              .build()
        };
    ShadowDisplayManager.setSupportedModes(displayId, modesToSet);

    Display.Mode[] modes = getGlobal().getRealDisplay(displayId).getSupportedModes();
    assertThat(modes).hasLength(modesToSet.length);
    assertThat(modes).asList().containsExactlyElementsIn(modesToSet);

    assertThat(events).containsExactly("Added " + displayId, "Changed " + displayId);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_defaultValue_shouldReturnOne() {
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(1.0f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToZero_shouldReturnZero() {
    instance.setSaturationLevel(0.0f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(0.0f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToHalf_shouldReturnHalf() {
    instance.setSaturationLevel(0.5f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(0.5f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToOne_shouldReturnOne() {
    instance.setSaturationLevel(0.0f);
    instance.setSaturationLevel(1.0f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(1.0f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToZeroViaShadow_shouldReturnZero() {
    shadowOf(instance).setSaturationLevel(0.0f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(0.0f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToHalfViaShadow_shouldReturnHalf() {
    shadowOf(instance).setSaturationLevel(0.5f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(0.5f);
  }

  @Test
  @Config(minSdk = P)
  public void getSaturationLevel_setToOneViaShadow_shouldReturnOne() {
    shadowOf(instance).setSaturationLevel(0.0f);
    shadowOf(instance).setSaturationLevel(1.0f);
    assertThat(shadowOf(instance).getSaturationLevel()).isEqualTo(1.0f);
  }

  @Test @Config(minSdk = P)
  public void setSaturationLevel_setToValueGreaterThanOne_shouldThrow() {
    try {
      instance.setSaturationLevel(1.1f);
      fail("Expected IllegalArgumentException thrown");
    } catch (IllegalArgumentException expected) {}
  }

  @Test @Config(minSdk = P)
  public void setSaturationLevel_setToNegativeValue_shouldThrow() {
    try {
      instance.setSaturationLevel(-0.1f);
      fail("Expected IllegalArgumentException thrown");
    } catch (IllegalArgumentException expected) {}
  }

  @Test @Config(minSdk = P)
  public void setSaturationLevel_setToValueGreaterThanOneViaShadow_shouldThrow() {
    try {
      shadowOf(instance).setSaturationLevel(1.1f);
      fail("Expected IllegalArgumentException thrown");
    } catch (IllegalArgumentException expected) {}
  }

  @Test @Config(minSdk = P)
  public void setSaturationLevel_setToNegativevalueViaShadow_shouldThrow() {
    try {
      shadowOf(instance).setSaturationLevel(-0.1f);
      fail("Expected IllegalArgumentException thrown");
    } catch (IllegalArgumentException expected) {}
  }

  @Test
  @Config(minSdk = P)
  public void getDefaultBrightnessConfiguration_notSetViaShadow_shouldReturnNull() {
    assertThat(instance.getDefaultBrightnessConfiguration()).isNull();
  }

  @Test
  @Config(minSdk = P)
  public void getDefaultBrightnessConfiguration_setViaShadow_shouldReturnValueSet() {
    BrightnessConfiguration config =
        new BrightnessConfiguration.Builder(
                /* lux= */ new float[] {0.0f, 5000.0f}, /* nits= */ new float[] {2.0f, 400.0f})
            .build();
    ShadowDisplayManager.setDefaultBrightnessConfiguration(config);
    assertThat(instance.getDefaultBrightnessConfiguration()).isEqualTo(config);
  }

  @Test
  @Config(minSdk = P)
  public void getBrightnessConfiguration_unset_shouldReturnDefault() {
    BrightnessConfiguration config =
        new BrightnessConfiguration.Builder(
                /* lux= */ new float[] {0.0f, 5000.0f}, /* nits= */ new float[] {2.0f, 400.0f})
            .build();
    ShadowDisplayManager.setDefaultBrightnessConfiguration(config);
    assertThat(instance.getBrightnessConfiguration()).isEqualTo(config);
  }

  @Test
  @Config(minSdk = P)
  public void getBrightnessConfiguration_setToNull_shouldReturnDefault() {
    BrightnessConfiguration config =
        new BrightnessConfiguration.Builder(
                /* lux= */ new float[] {0.0f, 5000.0f}, /* nits= */ new float[] {2.0f, 400.0f})
            .build();
    ShadowDisplayManager.setDefaultBrightnessConfiguration(config);
    instance.setBrightnessConfiguration(null);
    assertThat(instance.getBrightnessConfiguration()).isEqualTo(config);
  }

  @Test
  @Config(minSdk = P)
  public void getBrightnessConfiguration_setToValue_shouldReturnValue() {
    BrightnessConfiguration defaultConfig =
        new BrightnessConfiguration.Builder(
                /* lux= */ new float[] {0.0f, 5000.0f}, /* nits= */ new float[] {2.0f, 400.0f})
            .build();
    BrightnessConfiguration setConfig =
        new BrightnessConfiguration.Builder(
                /* lux= */ new float[] {0.0f, 2500.0f, 6000.0f},
                /* nits= */ new float[] {10.0f, 300.0f, 450.0f})
            .build();
    ShadowDisplayManager.setDefaultBrightnessConfiguration(defaultConfig);
    instance.setBrightnessConfiguration(setConfig);
    assertThat(instance.getBrightnessConfiguration()).isEqualTo(setConfig);
  }

  @Test
  @Config(minSdk = P)
  public void getBrightnessEvent_unset_shouldReturnEmpty() {
    assertThat(instance.getBrightnessEvents()).isEmpty();
  }

  @Test
  @Config(minSdk = Q)
  public void getBrightnessEvent_setToValue_shouldReturnValue() {
    List<BrightnessChangeEvent> events = new ArrayList<>();
    events.add(
        new BrightnessChangeEventBuilder()
            .setBrightness(230)
            .setTimeStamp(999123L)
            .setPackageName("somepackage.com")
            .setUserId(0)
            .setLuxValues(new float[] {1.0f, 2.0f, 3.0f, 4.0f})
            .setLuxTimestamps(new long[] {1000L, 2000L, 3000L, 4000L})
            .setBatteryLevel(0.8f)
            .setPowerBrightnessFactor(1.0f)
            .setNightMode(false)
            .setColorTemperature(0)
            .setLastBrightness(100)
            .setIsDefaultBrightnessConfig(true)
            .setUserBrightnessPoint(false)
            .setColorValues(new long[] {35L, 45L, 25L, 10L}, 10000L)
            .build());
    events.add(
        new BrightnessChangeEventBuilder()
            .setBrightness(1000)
            .setTimeStamp(1000123L)
            .setPackageName("anotherpackage.com")
            .setUserId(0)
            .setLuxValues(new float[] {1.0f, 2.0f, 3.0f, 4.0f})
            .setLuxTimestamps(new long[] {1000L, 2000L, 3000L, 4000L})
            .setBatteryLevel(0.8f)
            .setPowerBrightnessFactor(1.0f)
            .setNightMode(false)
            .setColorTemperature(0)
            .setLastBrightness(300)
            .setIsDefaultBrightnessConfig(true)
            .setUserBrightnessPoint(true)
            .setColorValues(new long[] {35L, 45L, 25L, 10L}, 10000L)
            .build());

    ShadowDisplayManager.setBrightnessEvents(events);
    assertThat(instance.getBrightnessEvents()).containsExactlyElementsIn(events);
  }

  // because DisplayManagerGlobal don't exist in Jelly Bean,
  // and we don't want them resolved as part of the test class.
  static class HideFromJB {
    public static DisplayManagerGlobal getGlobal() {
      return DisplayManagerGlobal.getInstance();
    }
  }

  private static class MyDisplayListener implements DisplayManager.DisplayListener {
    private final List<String> events;

    MyDisplayListener(List<String> events) {
      this.events = events;
    }

    @Override
    public void onDisplayAdded(int displayId) {
      events.add("Added " + displayId);
    }

    @Override
    public void onDisplayRemoved(int displayId) {
      events.add("Removed " + displayId);
    }

    @Override
    public void onDisplayChanged(int displayId) {
      events.add("Changed " + displayId);
    }
  }
}
