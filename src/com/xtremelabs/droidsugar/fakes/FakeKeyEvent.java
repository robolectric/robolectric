package com.xtremelabs.droidsugar.fakes;

import android.view.KeyEvent;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(KeyEvent.class)
public class FakeKeyEvent {
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
