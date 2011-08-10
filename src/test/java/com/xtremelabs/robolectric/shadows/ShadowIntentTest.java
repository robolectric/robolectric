package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowIntentTest {
    @Test
    public void testToUri()throws Exception {
        Intent intent = new Intent();
        shadowOf(intent).setURI("http://foo");
        assertThat(intent.toURI(), is("http://foo"));
    }
}
