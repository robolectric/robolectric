package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowAccessibilityServiceTest {
  private MyService service ;
  private ShadowAccessibilityService shadow;

  @Before
  public void setup() {
    service = Robolectric.setupService(MyService.class);
    shadow = shadowOf(service);
  }

  /**
   * After performing a global action, it should be recorded.
   */
  @Test
  public void shouldRecordPerformedAction(){
    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    assertThat(shadow.getGlobalActionsPerformed().size()).isEqualTo(1);
    assertThat(shadow.getGlobalActionsPerformed().get(0)).isEqualTo(1);
  }

  public static class MyService extends AccessibilityService {
    @Override
    public void onDestroy() {
      super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {
      //Do nothing
    }

    @Override
    public void onInterrupt() {
      //Do nothing
    }
  }
}

