package com.xtremelabs.robolectric.fakes;

import android.widget.EditText;
import com.xtremelabs.robolectric.RobolectricAndroidTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricAndroidTestRunner.class)
public class EditTextTest {
    @Before public void setUp() throws Exception {
        RobolectricAndroidTestRunner.addGenericProxies();
    }

    @Test
    public void shouldNotBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(null).isFocusable());
    }
}
