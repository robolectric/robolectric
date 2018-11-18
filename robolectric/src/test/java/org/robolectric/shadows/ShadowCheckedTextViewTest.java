package org.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.widget.CheckedTextView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowCheckedTextViewTest {

  private CheckedTextView checkedTextView;

  @Before
  public void beforeTests() {
    checkedTextView = new CheckedTextView(ApplicationProvider.getApplicationContext());
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
    CheckedTextView view = new CheckedTextView(ApplicationProvider.getApplicationContext());
    assertFalse(view.isChecked());
    view.toggle();
    assertTrue(view.isChecked());
    view.toggle();  // Used to support performClick(), but Android doesn't. Sigh.
    assertFalse(view.isChecked());
  }
}
