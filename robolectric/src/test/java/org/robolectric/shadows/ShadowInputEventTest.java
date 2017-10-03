package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.view.InputDevice;
import android.view.KeyEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowInputEventTest {
  @Test
  public void canSetInputDeviceOnKeyEvent() throws Exception {
    InputDevice myDevice = ShadowInputDevice.makeInputDeviceNamed("myDevice");
    KeyEvent keyEvent = new KeyEvent(1, 2);
    shadowOf(keyEvent).setDevice(myDevice);
    assertThat(keyEvent.getDevice().getName()).isEqualTo("myDevice");
  }
}
