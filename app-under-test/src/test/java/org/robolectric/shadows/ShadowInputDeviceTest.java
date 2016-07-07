package org.robolectric.shadows;

import android.view.InputDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowInputDeviceTest {
  @Test
  public void canConstructInputDeviceWithName() throws Exception {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    assertThat(inputDevice.getName()).isEqualTo("foo");
  }
}
