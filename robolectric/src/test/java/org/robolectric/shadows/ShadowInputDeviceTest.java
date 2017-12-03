package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.InputDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowInputDeviceTest {
  @Test
  public void canConstructInputDeviceWithName() throws Exception {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    assertThat(inputDevice.getName()).isEqualTo("foo");
  }
}
