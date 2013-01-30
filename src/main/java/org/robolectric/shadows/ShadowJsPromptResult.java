package com.xtremelabs.robolectric.shadows;

import android.webkit.JsPromptResult;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(JsPromptResult.class)
public class ShadowJsPromptResult extends ShadowJsResult{

    public static JsPromptResult newInstance() {
        return Robolectric.newInstanceOf(JsPromptResult.class);
    }
}
