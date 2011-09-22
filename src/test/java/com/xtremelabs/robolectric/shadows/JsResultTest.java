package com.xtremelabs.robolectric.shadows;

import android.webkit.JsResult;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(WithTestDefaultsRunner.class)
public class JsResultTest {

    @Test
    public void shouldRecordCanceled() throws Exception {
        JsResult jsResult = Robolectric.newInstanceOf(JsResult.class);

        assertFalse(shadowOf(jsResult).wasCancelled());

        jsResult.cancel();
        assertTrue(shadowOf(jsResult).wasCancelled());

    }

}
