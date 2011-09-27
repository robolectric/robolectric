package com.xtremelabs.robolectric.shadows;

import android.webkit.JsPromptResult;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class JsPromptResultTest {

    @Test
    public void shouldConstruct() throws Exception {
        JsPromptResult result = ShadowJsPromptResult.newInstance();
        assertNotNull(result);
    }
}
