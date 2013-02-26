package org.robolectric.shadows;

import android.webkit.JsResult;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(JsResult.class)
public class ShadowJsResult {

    private boolean wasCancelled;

    @Implementation
    public void cancel() {
        wasCancelled = true;
    }

    public boolean wasCancelled() {
        return wasCancelled;
    }
}
