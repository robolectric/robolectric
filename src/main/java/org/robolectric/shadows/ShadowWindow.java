package org.robolectric.shadows;

import android.view.Window;
import android.view.WindowManager;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
    @Implementation
    public WindowManager.LayoutParams getAttributes() {
        return new WindowManager.LayoutParams();
    }
}
