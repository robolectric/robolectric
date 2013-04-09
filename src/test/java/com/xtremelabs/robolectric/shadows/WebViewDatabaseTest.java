package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class WebViewDatabaseTest {
    @Test
    public void shouldBeSingleton() throws Exception {
        assertTrue(ShadowWebViewDatabase.getInstance(new Activity()) == ShadowWebViewDatabase.getInstance(new Activity()));
        assertTrue(ShadowWebViewDatabase.getInstance(new Activity()) != null);
    }
}
