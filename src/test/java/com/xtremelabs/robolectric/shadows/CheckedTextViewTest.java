package com.xtremelabs.robolectric.shadows;

import android.widget.CheckedTextView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class CheckedTextViewTest {
    @Test public void toggle_shouldChangeCheckedness() throws Exception {
        CheckedTextView view = new CheckedTextView(null);
        assertFalse(view.isChecked());
        view.toggle();
        assertTrue(view.isChecked());
        view.performClick();
        assertFalse(view.isChecked());
    }
}
