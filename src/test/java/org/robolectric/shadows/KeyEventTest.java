package org.robolectric.shadows;

import android.view.InputDevice;
import android.view.KeyEvent;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class KeyEventTest {
    @Test
    public void canSetInputDeviceOnKeyEvent() throws Exception {
        InputDevice myDevice = ShadowInputDevice.makeInputDeviceNamed("myDevice");
        KeyEvent keyEvent = new KeyEvent(1, 2);
        shadowOf(keyEvent).setDevice(myDevice);
        assertThat(keyEvent.getDevice().getName()).isEqualTo("myDevice");
    }
}
