package com.xtremelabs.robolectric.shadows;

import android.view.InputDevice;
import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(TestRunners.WithDefaults.class)
public class InputDeviceTest {
    @Test
    public void canConstructInputDeviceWithName() throws Exception {
        InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
        assertThat(inputDevice.getName(), equalTo("foo"));
    }
}
