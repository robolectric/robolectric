package com.xtremelabs.robolectric.shadows;

import android.view.InputDevice;
import android.view.KeyEvent;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class KeyEventTest {
    @Test
    public void canSetInputDeviceOnKeyEvent() throws Exception {
        InputDevice myDevice = ShadowInputDevice.makeInputDeviceNamed("myDevice");
        KeyEvent keyEvent = new KeyEvent(1, 2);
        shadowOf(keyEvent).setDevice(myDevice);
        assertThat(keyEvent.getDevice().getName(), equalTo("myDevice"));
    }
}
