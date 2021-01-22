package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.SoftKeyboardController;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Test for ShadowSoftKeyboardController. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public final class ShadowSoftKeyboardControllerTest {

  private MyService myService;
  private SoftKeyboardController softKeyboardController;

  @Before
  public void setUp() {
    myService = Robolectric.setupService(MyService.class);
    softKeyboardController = myService.getSoftKeyboardController();
  }

  @Test
  public void getShowMode_default_returnsAuto() {
    int showMode = softKeyboardController.getShowMode();

    assertThat(showMode).isEqualTo(AccessibilityService.SHOW_MODE_AUTO);
  }

  @Test
  public void setShowMode_updatesShowMode() {
    int newMode = AccessibilityService.SHOW_MODE_HIDDEN;

    softKeyboardController.setShowMode(newMode);

    assertThat(softKeyboardController.getShowMode()).isEqualTo(newMode);
  }

  @Test
  public void addOnShowModeChangedListener_registersListener() {
    int newMode = AccessibilityService.SHOW_MODE_HIDDEN;
    TestOnShowModeChangedListener listener = new TestOnShowModeChangedListener();

    softKeyboardController.addOnShowModeChangedListener(listener);

    softKeyboardController.setShowMode(newMode);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(listener.invoked).isTrue();
    assertThat(listener.showMode).isEqualTo(newMode);
  }

  @Test
  public void removeOnShowModeChangedListener_unregistersListener() {
    TestOnShowModeChangedListener listener = new TestOnShowModeChangedListener();
    softKeyboardController.addOnShowModeChangedListener(listener);

    softKeyboardController.removeOnShowModeChangedListener(listener);

    softKeyboardController.setShowMode(AccessibilityService.SHOW_MODE_HIDDEN);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(listener.invoked).isFalse();
  }

  @Test
  public void removeOnShowModeChangedListener_listenerNotRegistered_returnsFalse() {
    TestOnShowModeChangedListener listener = new TestOnShowModeChangedListener();

    assertThat(softKeyboardController.removeOnShowModeChangedListener(listener)).isFalse();
  }

  @Test
  public void removeOnShowModeChangedListener_listenerRegistered_returnsTrue() {
    TestOnShowModeChangedListener listener = new TestOnShowModeChangedListener();
    softKeyboardController.addOnShowModeChangedListener(listener);

    assertThat(softKeyboardController.removeOnShowModeChangedListener(listener)).isTrue();
  }

  /** Test listener that records when it is invoked. */
  private static class TestOnShowModeChangedListener
      implements SoftKeyboardController.OnShowModeChangedListener {
    private boolean invoked = false;
    private int showMode = -1;

    @Override
    public void onShowModeChanged(SoftKeyboardController controller, int showMode) {
      this.invoked = true;
      this.showMode = showMode;
    }
  }

  /** Empty implementation of AccessibilityService, for test purposes. */
  private static class MyService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {
      // Do nothing
    }

    @Override
    public void onInterrupt() {
      // Do nothing
    }
  }
}
