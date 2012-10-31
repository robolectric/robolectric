package com.xtremelabs.robolectric.shadows;

import android.view.InputDevice;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(WithTestDefaultsRunner.class)
public class InputDeviceTest {
    @Test
    public void canConstructInputDeviceWithName() throws Exception {
        InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
        assertThat(inputDevice.getName(), equalTo("foo"));
    }
}
