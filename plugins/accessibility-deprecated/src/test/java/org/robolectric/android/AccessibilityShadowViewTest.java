package org.robolectric.android;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Rect;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.AccessibilityChecks;

@RunWith(AndroidJUnit4.class)
public class AccessibilityShadowViewTest {

  @Test
  @AccessibilityChecks
  public void checkedClick_withA11yChecksAnnotation_shouldThrow() throws Exception {
    AccessibilityUtil.setThrowExceptionForErrors(true);

    View unlabeledView = new View(RuntimeEnvironment.application);
    unlabeledView.setContentDescription(null);
    unlabeledView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    unlabeledView.setClickable(true);
    Rect validViewBounds = new Rect(100, 100, 200, 200);
    shadowOf(unlabeledView).setGlobalVisibleRect(validViewBounds);

    try {
      shadowOf(unlabeledView).checkedPerformClick();
      fail("RuntimeException not thrown");
    } catch (RuntimeException e) {
      // expected
      assertContains("View is not visible and cannot be clicked", e.getMessage());
    }
  }

  private void assertContains(String expectedText, String actualText) {
    assertTrue("Expected <" + actualText + "> to contain <"
        + expectedText + ">", actualText.contains(expectedText));
  }
}
