package com.xtremelabs.robolectric.shadows;

import android.widget.EditText;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class EditTextTest {
    @Before public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
    }

    @Test
    public void shouldBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(null).isFocusable());
        assertTrue(new EditText(null).isFocusableInTouchMode());
    }
}
