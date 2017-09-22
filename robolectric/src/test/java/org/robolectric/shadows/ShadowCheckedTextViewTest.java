package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.widget.CheckedTextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowCheckedTextViewTest {

  private CheckedTextView checkedTextView;

  @Before
  public void beforeTests() {
    checkedTextView = new CheckedTextView(RuntimeEnvironment.application);
  }

  @Test
  public void testToggle() {
    assertFalse(checkedTextView.isChecked());

    checkedTextView.toggle();

    assertTrue(checkedTextView.isChecked());
  }

  @Test
  public void testSetChecked() {
    assertFalse(checkedTextView.isChecked());

    checkedTextView.setChecked(true);

    assertTrue(checkedTextView.isChecked());
  }

  @Test public void toggle_shouldChangeCheckedness() throws Exception {
    CheckedTextView view = new CheckedTextView(RuntimeEnvironment.application);
    assertFalse(view.isChecked());
    view.toggle();
    assertTrue(view.isChecked());
    view.toggle();  // Used to support performClick(), but Android doesn't. Sigh.
    assertFalse(view.isChecked());
  }
}
