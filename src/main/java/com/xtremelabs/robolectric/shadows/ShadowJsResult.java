package com.xtremelabs.robolectric.shadows;

import android.webkit.JsResult;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

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
