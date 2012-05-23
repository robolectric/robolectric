package com.xtremelabs.robolectric.shadows;

import android.text.method.LinkMovementMethod;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class LinkMovementMethodTest {

    @Test
    public void getInstance_shouldReturnAnInstanceOf_LinkedMovementMethod() throws Exception {
        assertThat(LinkMovementMethod.getInstance(), instanceOf(LinkMovementMethod.class));
    }

}
