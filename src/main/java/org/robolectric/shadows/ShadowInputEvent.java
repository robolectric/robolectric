package com.xtremelabs.robolectric.shadows;

import android.view.InputDevice;
import android.view.InputEvent;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(InputEvent.class)
public class ShadowInputEvent {
    protected InputDevice device;

    @Implementation
    public InputDevice getDevice() {
        return device;
    }

    public void setDevice(InputDevice device) {
        this.device = device;
    }
}
