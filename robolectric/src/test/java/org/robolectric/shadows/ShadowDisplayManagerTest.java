package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.robolectric.shadows.ShadowDisplayManagerTest.HideFromJB.createDisplayInfo;
import static org.robolectric.shadows.ShadowDisplayManagerTest.HideFromJB.getGlobal;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.view.Display;
import android.view.DisplayInfo;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowDisplayManagerTest {

  private DisplayManager instance;

  @Before
  public void setUp() throws Exception {
    instance = (DisplayManager) RuntimeEnvironment.application
        .getSystemService(Context.DISPLAY_SERVICE);
  }

  @Test @Config(maxSdk = JELLY_BEAN)
  public void notSupportedInJellyBean() throws Exception {
    assertThatThrownBy(() -> ShadowDisplayManager.removeDisplay(0))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("displays not supported in Jelly Bean");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getDisplayInfo_shouldReturnCopy() throws Exception {
    DisplayInfo displayInfo = getGlobal().getDisplayInfo(Display.DEFAULT_DISPLAY);
    int origAppWidth = displayInfo.appWidth;
    displayInfo.appWidth++;
    assertThat(getGlobal().getDisplayInfo(Display.DEFAULT_DISPLAY).appWidth)
        .isEqualTo(origAppWidth);
  }

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void addDisplay() throws Exception {
    DisplayInfo displayInfo = createDisplayInfo(100, 200);
    int displayId = ShadowDisplayManager.addDisplay(displayInfo);
    assertThat(displayId).isGreaterThan(0);

    DisplayInfo di = getGlobal().getDisplayInfo(displayId);
    assertThat(di.appWidth).isEqualTo(100);
    assertThat(di.appHeight).isEqualTo(200);

    Display display = instance.getDisplay(displayId);
    assertThat(display.getDisplayId()).isEqualTo(displayId);
  }

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void addDisplay_shouldNotifyListeners() throws Exception {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), null);
    DisplayInfo displayInfo = createDisplayInfo(100, 200);
    int displayId = ShadowDisplayManager.addDisplay(displayInfo);
    assertThat(events).containsExactly("Added " + displayId);
  }

  @Test @Config(minSdk = JELLY_BEAN_MR2)
  public void changeAndRemoveDisplay_shouldNotifyListeners() throws Exception {
    List<String> events = new ArrayList<>();
    instance.registerDisplayListener(new MyDisplayListener(events), null);
    DisplayInfo displayInfo = createDisplayInfo(100, 200);
    int displayId = ShadowDisplayManager.addDisplay(displayInfo);

    ShadowDisplayManager.changeDisplay(displayId, createDisplayInfo(300, 400));

    assertThat(getGlobal().getRealDisplay(displayId).getWidth()).isEqualTo(300);

    ShadowDisplayManager.removeDisplay(displayId);

    assertThat(events).containsExactly(
        "Added " + displayId,
        "Changed " + displayId,
        "Removed " + displayId);
  }

  // because DisplayInfo and DisplayManagerGlobal don't exist in Jelly Bean,
  // and we don't want them resolved as part of the test class.
  static class HideFromJB {
    static DisplayInfo createDisplayInfo(int width, int height) {
      DisplayInfo displayInfo = new DisplayInfo();
      displayInfo.appWidth = width;
      displayInfo.appHeight = height;
      return displayInfo;
    }

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
