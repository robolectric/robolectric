package com.xtremelabs.droidsugar.fakes;

import android.view.KeyEvent;
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

    public final int getAction() {
        return action;
    }

    public final int getKeyCode() {
        return code;
    }
}
