package com.xtremelabs.robolectric.shadows;

import android.view.Window;
import android.view.WindowManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
    @Implementation
    public WindowManager.LayoutParams getAttributes() {
        return new WindowManager.LayoutParams();
    }
}
