package com.xtremelabs.robolectric.shadows;

import android.widget.ImageButton;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ImageButtonTest {
    @Test
    public void testBackground() throws Exception {
        ImageButton button = new ImageButton(Robolectric.application, new TestAttributeSet(new ArrayList<Attribute>(), null, null));
        assertThat(button.getBackground(), notNullValue());
    }
}
