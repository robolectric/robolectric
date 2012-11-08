package com.xtremelabs.robolectric.shadows;

import android.view.InputDevice;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(InputDevice.class)
public class ShadowInputDevice {
    private String deviceName;

    public static InputDevice makeInputDeviceNamed(String deviceName) {
        InputDevice inputDevice = Robolectric.newInstanceOf(InputDevice.class);
        shadowOf(inputDevice).setDeviceName(deviceName);
        return inputDevice;
    }

    @Implementation
    public String getName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
