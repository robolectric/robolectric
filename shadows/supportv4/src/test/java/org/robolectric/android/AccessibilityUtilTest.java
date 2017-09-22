package org.robolectric.android;

import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Rect;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.apps.common.testing.accessibility.framework.integrations.AccessibilityViewCheckException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.AccessibilityChecks;
import org.robolectric.annotation.AccessibilityChecks.ForRobolectricVersion;
import org.robolectric.util.TestRunnerWithManifest;

/**
 * Tests for accessibility checking. The checking relies on the Accessibility Test Framework for
 * Android, which has support-v4 as a dependency, so these tests are included where the presence
 * of that library is guaranteed.
 */
@RunWith(TestRunnerWithManifest.class)
public class AccessibilityUtilTest {
  private static final String DUPLICATE_STRING = "Duplicate";
  private TextView textViewWithClickableSpan;
  private LinearLayout parentLayout;
  private View labeledView;
  private View unlabeledView;
  
  @Before
  public void setUp() throws Exception {
    Rect validViewBounds = new Rect(100, 100, 200, 200);
    
    // Set the statics back to their default values
    AccessibilityUtil.setRunChecksForRobolectricVersion(null);
    AccessibilityUtil.setSuppressingResultMatcher(null);
    AccessibilityUtil.setRunChecksFromRootView(false);
    AccessibilityUtil.setThrowExceptionForErrors(true);

    labeledView = new View(RuntimeEnvironment.application);
    labeledView.setContentDescription("Something");
    labeledView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    labeledView.setClickable(true);
    // Force the views on the screen so they'll be seen as visible even though they aren't
    // part of a valid view hierarchy
    shadowOf(labeledView).setGlobalVisibleRect(validViewBounds);

    unlabeledView = new View(RuntimeEnvironment.application);
    unlabeledView.setContentDescription(null);
    unlabeledView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    unlabeledView.setClickable(true);
    shadowOf(unlabeledView).setGlobalVisibleRect(validViewBounds);

    parentLayout = new LinearLayout(RuntimeEnvironment.application);
    parentLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    parentLayout.addView(labeledView);
    shadowOf(parentLayout).setGlobalVisibleRect(validViewBounds);
    
    textViewWithClickableSpan = new TextView(RuntimeEnvironment.application);
    SpannableString spannableString = new SpannableString("Some text");
    ClickableSpan mockClickableSpan = mock(ClickableSpan.class);
    spannableString.setSpan(mockClickableSpan, 0, 1, Spanned.SPAN_COMPOSING);
    textViewWithClickableSpan.setText(spannableString);
    shadowOf(textViewWithClickableSpan).setGlobalVisibleRect(validViewBounds);
  }

  @Test(expected = AccessibilityViewCheckException.class)
  public void checkUnlabeledView_shouldThrow() throws Exception {
    AccessibilityUtil.checkView(unlabeledView);
  }

  @Test
  public void checkOKView_shouldNotThrow() throws Exception {
    AccessibilityUtil.checkView(labeledView);
  }

  @Test
  public void default_viewWithSiblingIssue_shouldNotThrow() throws Exception {
    parentLayout.addView(unlabeledView);
    AccessibilityUtil.checkView(labeledView);
  }

  @Test(expected = AccessibilityViewCheckException.class)
  public void whenCheckingFromRoot_viewWithSiblingIssue_shouldThrow() throws Exception {
    parentLayout.addView(unlabeledView);
    AccessibilityUtil.setRunChecksFromRootView(true);
    AccessibilityUtil.checkView(labeledView);
  }
  
  @Test(expected = AccessibilityViewCheckException.class)
  @AccessibilityChecks
  public void whenAnnotationPresent_conditionalCheckRun() {
    AccessibilityUtil.checkViewIfCheckingEnabled(unlabeledView);
  }

  @Test
  public void whenAnnotationNotPresent_conditionalCheckNotRun() {
    AccessibilityUtil.checkViewIfCheckingEnabled(unlabeledView);
  }

  @Test(expected = AccessibilityViewCheckException.class)
  public void framework2pt0Error_byDefault_shouldThrow() throws Exception {
    AccessibilityUtil.checkView(textViewWithClickableSpan);
  }

  @Test
  public void framework2pt0Error_whenCheckingForRL3pt0_shouldNotThrow() throws Exception {
    AccessibilityUtil.setRunChecksForRobolectricVersion(ForRobolectricVersion.VERSION_3_0);
    AccessibilityUtil.checkView(textViewWithClickableSpan);
  }

  @Test
  @AccessibilityChecks(forRobolectricVersion = ForRobolectricVersion.VERSION_3_0)
  public void framework2pt0Error_annotationForRL3pt0_shouldNotThrow() throws Exception {
    AccessibilityUtil.checkView(textViewWithClickableSpan);
  }

  @Test(expected = AccessibilityViewCheckException.class)
  @AccessibilityChecks(forRobolectricVersion = ForRobolectricVersion.VERSION_3_0)
  public void framework2pt0Error_codeForcesRL3pt1_shouldThrow() throws Exception {
    AccessibilityUtil.setRunChecksForRobolectricVersion(ForRobolectricVersion.VERSION_3_1);
    AccessibilityUtil.checkView(textViewWithClickableSpan);
  }

  @Test
  public void whenSuppressingResults_shouldNotThrow() throws Exception {
    AccessibilityUtil.setSuppressingResultMatcher(Matchers.anything());
    AccessibilityUtil.checkView(unlabeledView);
  }

  @Test
  public void whenOnlyPrintingResults_shouldNotThrow() throws Exception {
    AccessibilityUtil.setThrowExceptionForErrors(false);
    AccessibilityUtil.checkView(unlabeledView);
  }

  @Test
  public void warningIssue_shouldNotThrow() throws Exception {
    labeledView.setContentDescription(DUPLICATE_STRING);
    parentLayout.setContentDescription(DUPLICATE_STRING);
    parentLayout.setClickable(true);
    AccessibilityUtil.checkView(parentLayout);
  }

}

