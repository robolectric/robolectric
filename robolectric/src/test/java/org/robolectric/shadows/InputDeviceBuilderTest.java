package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.view.InputDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.versioning.AndroidVersions.U;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = U.SDK_INT)
public class InputDeviceBuilderTest {

  @Test
  public void testBuild() {
    final InputDevice device =
        InputDeviceBuilder.newBuilder()
            .setId(2)
            .setControllerNumber(43)
            .setName("Test Device ")
            .setVendorId(44)
            .setProductId(45)
            .setDescriptor("descriptor")
            .setExternal(true)
            .setSources(InputDevice.SOURCE_HDMI)
            .setKeyboardType(InputDevice.KEYBOARD_TYPE_NON_ALPHABETIC)
            .setHasVibrator(true)
            .setHasMicrophone(true)
            .build();

    assertDevice(device);
  }

  private void assertDevice(InputDevice device) {
    assertThat(device.getId()).isEqualTo(2);

    assertThat(device.getControllerNumber()).isEqualTo(43);
    assertThat(device.getName()).isEqualTo("Test Device ");
    assertThat(device.getVendorId()).isEqualTo(44);
    assertThat(device.getProductId()).isEqualTo(45);
    assertThat(device.getDescriptor()).isEqualTo("descriptor");

    assertThat(device.getSources()).isEqualTo(InputDevice.SOURCE_HDMI);
    assertThat(device.getKeyboardType()).isEqualTo(InputDevice.KEYBOARD_TYPE_NON_ALPHABETIC);
    assertThat(device.getVibrator().hasVibrator()).isEqualTo(true);
    assertThat(device.hasMicrophone()).isEqualTo(true);
  }

  // regression test that directly using the platform InputDevice.Builder is functioning correctly.
  @Test
  public void platformBuilder() {
    final InputDevice device =
        new InputDevice.Builder()
            .setId(2)
            .setControllerNumber(43)
            .setName("Test Device ")
            .setVendorId(44)
            .setProductId(45)
            .setDescriptor("descriptor")
            .setExternal(true)
            .setSources(InputDevice.SOURCE_HDMI)
            .setKeyboardType(InputDevice.KEYBOARD_TYPE_NON_ALPHABETIC)
            .setHasVibrator(true)
            .setHasMicrophone(true)
            .build();

    assertDevice(device);
  }
}
