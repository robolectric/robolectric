package com.xtremelabs.robolectric.shadows;

import android.view.KeyEvent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(KeyEvent.class)
public class ShadowKeyEvent extends ShadowInputEvent {
    private int action;
    private int code;

    public void __constructor__(int action, int code) {
        this.action = action;
        this.code = code;
    }

    @Implementation
    public final int getAction() {
        return action;
    }

    @Implementation
    public final int getKeyCode() {
        return code;
    }
}
