package org.robolectric.shadows;

import android.view.ContextThemeWrapper;
import android.view.Window;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.tester.android.view.RoboWindow;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Window.class)
public class ShadowWindow {
    @RealObject private Window realWindow;

    private int flags;

    public static Window create(ContextThemeWrapper activity) {
        return new RoboWindow(activity);
    }

    @Implementation
    public void setFlags(int flags, int mask) {
        this.flags = (this.flags & ~mask) | (flags & mask);
        directlyOn(realWindow, Window.class, "setFlags", int.class, int.class).invoke(flags, mask);
    }

    public boolean getFlag(int flag) {
        return (flags & flag) == flag;
    }

    public void performLayout() {
        ((RoboWindow) realWindow).performLayout();
    }
}
