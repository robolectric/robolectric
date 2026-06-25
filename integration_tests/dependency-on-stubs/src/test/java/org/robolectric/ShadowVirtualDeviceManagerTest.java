package org.robolectric;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.companion.virtual.VirtualDeviceManager;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowVirtualDeviceManager;
import org.robolectric.shadows.VirtualDeviceParamsBuilder;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VANILLA_ICE_CREAM)
public class ShadowVirtualDeviceManagerTest {

  /**
   * This is a regression test to ensure that VirtualDevice Objects can be created using
   * ShadowVirtualDeviceManager without getting class file errors as
   * VirtualDeviceManager$VirtualDevice is not in the public stubs jar.
   */
  @Test
  public void createVirtualDevice() {
    VirtualDeviceManager virtualDeviceManager =
        (VirtualDeviceManager)
            RuntimeEnvironment.getApplication().getSystemService(Context.VIRTUAL_DEVICE_SERVICE);

    ShadowVirtualDeviceManager shadowVirtualDeviceManager = Shadow.extract(virtualDeviceManager);
    Object virtualDevice = shadowVirtualDeviceManager.createVirtualDevice(100, null);
    ShadowVirtualDeviceManager.ShadowVirtualDevice shadowVirtualDevice =
        Shadow.extract(virtualDevice);
    assertThat(shadowVirtualDevice).isNotNull();
  }

  @Test
  public void virtualDeviceParams_canBeBuilt() {
    Object virtualDeviceParams = VirtualDeviceParamsBuilder.newBuilder().setName("sdfsdf").build();
    assertThat(virtualDeviceParams).isNotNull();

    // Check the legacy constructor still works
    virtualDeviceParams = new VirtualDeviceParamsBuilder().setName("sdfsdf").build();
    assertThat(virtualDeviceParams).isNotNull();
  }
}
