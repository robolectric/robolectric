package org.robolectric.shadows;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
    private int flags;
    private Context context;

    public void __constructor__(android.content.Context context) {
        this.context = context;
    }

    @Implementation
    public Context getContext() {
        return context;
    }

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
