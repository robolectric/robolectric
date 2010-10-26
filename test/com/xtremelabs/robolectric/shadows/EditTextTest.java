package com.xtremelabs.robolectric.shadows;

import android.widget.EditText;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(DogfoodRobolectricTestRunner.class)
public class EditTextTest {
    @Before public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();
    }

    @Test
    public void shouldNotBeFocusableByDefault() throws Exception {
        assertTrue(new EditText(null).isFocusable());
    }
}
