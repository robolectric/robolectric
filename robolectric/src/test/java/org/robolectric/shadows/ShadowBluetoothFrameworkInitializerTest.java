package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import android.bluetooth.BluetoothFrameworkInitializer;
import android.os.BluetoothServiceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TIRAMISU)
public class ShadowBluetoothFrameworkInitializerTest {
  @Test
  public void init() {
    BluetoothServiceManager instance1 = BluetoothFrameworkInitializer.getBluetoothServiceManager();
    assertThat(instance1).isNotNull();
    // verify result is cached
    assertThat(BluetoothFrameworkInitializer.getBluetoothServiceManager())
        .isSameInstanceAs(instance1);
    // verify instance can be reset between tests
    ShadowBluetoothFrameworkInitializer.reset();
    assertThat(BluetoothFrameworkInitializer.getBluetoothServiceManager())
        .isNotSameInstanceAs(instance1);
  }
}
