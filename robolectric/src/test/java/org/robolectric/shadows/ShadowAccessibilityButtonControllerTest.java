package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityButtonController;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestAccessibilityService;

/** Unit tests for {@link ShadowAccessibilityButtonController}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ShadowAccessibilityButtonControllerTest {

  private AccessibilityButtonController accessibilityButtonController;

  private boolean isClicked;

  @Before
  public void setUp() {
    TestAccessibilityService service = Robolectric.setupService(TestAccessibilityService.class);
    accessibilityButtonController = service.getAccessibilityButtonController();
  }

  @Test
  public void shouldAccessibilityButtonClickedTriggered() {
    createAndRegisterAccessibilityButtonCallback();
    shadowOf(accessibilityButtonController).performAccessibilityButtonClick();
    assertThat(isClicked).isTrue();
  }

  private void createAndRegisterAccessibilityButtonCallback() {
    isClicked = false;
    AccessibilityButtonController.AccessibilityButtonCallback accessibilityButtonCallback =
        new AccessibilityButtonController.AccessibilityButtonCallback() {
          @Override
          public void onClicked(AccessibilityButtonController controller) {
            isClicked = true;
          }
        };
    accessibilityButtonController.registerAccessibilityButtonCallback(accessibilityButtonCallback);
  }
}
