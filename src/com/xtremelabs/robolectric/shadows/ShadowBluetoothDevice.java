package com.xtremelabs.robolectric.shadows;

import android.bluetooth.BluetoothDevice;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(BluetoothDevice.class)
public class ShadowBluetoothDevice {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Implementation
    public String getName() {
        return name;
    }
}
