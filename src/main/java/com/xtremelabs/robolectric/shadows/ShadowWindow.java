package com.xtremelabs.robolectric.shadows;

import android.view.Window;
import android.view.WindowManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
    private int flags;

    @Implementation
    public WindowManager.LayoutParams getAttributes() {
        return new WindowManager.LayoutParams();
    }

    @Implementation
    public void setFlags(int flags, int mask) {
        this.flags = (this.flags & ~mask) | (flags & mask);
    }

    public boolean getFlag(int flag) {
        return (flags & flag) == flag;
    }
}
