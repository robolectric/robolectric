package com.xtremelabs.robolectric.shadows;

import android.webkit.WebView;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class WebViewTest {

    @Test
    public void shouldCreate() {
        assertNotNull(new WebView(null));
    }
}
