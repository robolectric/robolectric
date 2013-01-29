package org.robolectric.shadows;

import android.webkit.JsPromptResult;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class JsPromptResultTest {

    @Test
    public void shouldConstruct() throws Exception {
        JsPromptResult result = ShadowJsPromptResult.newInstance();
        assertNotNull(result);
    }
}
