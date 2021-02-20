package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;

import android.view.InputDevice;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowInputDeviceTest {
  @Test
  public void canConstructInputDeviceWithName() {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    assertThat(inputDevice.getName()).isEqualTo("foo");
  }

  @Test
  @Config(minSdk = KITKAT)
  public void canChangeProductId() {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    ShadowInputDevice shadowInputDevice = Shadow.extract(inputDevice);
    shadowInputDevice.setProductId(1337);

    assertThat(inputDevice.getProductId()).isEqualTo(1337);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void canChangeVendorId() {
    InputDevice inputDevice = ShadowInputDevice.makeInputDeviceNamed("foo");
    ShadowInputDevice shadowInputDevice = Shadow.extract(inputDevice);
    shadowInputDevice.setVendorId(1337);

    assertThat(inputDevice.getVendorId()).isEqualTo(1337);
  }

  @Test
  public void getDeviceIds() {
    int[] deviceIds = InputDevice.getDeviceIds();
    assertThat(deviceIds).hasLength(0);
  }
}
