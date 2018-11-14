package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.InputDevice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowInputDeviceTest {
  @Test
  public void canConstructInputDeviceWithName() throws Exception {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    assertThat(inputDevice.getName()).isEqualTo("foo");
  }
}
