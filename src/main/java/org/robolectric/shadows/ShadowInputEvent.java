package org.robolectric.shadows;

import android.view.InputDevice;
import android.view.InputEvent;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

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
