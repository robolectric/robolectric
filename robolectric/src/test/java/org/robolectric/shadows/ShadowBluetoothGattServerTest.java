package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBluetoothGattServer} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowBluetoothGattServerTest {
  private static final UUID TEST_UUID_1 = UUID.fromString("51F4E3A0-FFFF-479A-BA78-4F042BB2CEF0");
  private static final UUID TEST_UUID_2 = UUID.fromString("52F4E3A0-FFFF-479A-BA78-4F042BB2CEF0");
  private static final UUID TEST_UUID_3 = UUID.fromString("53F4E3A0-FFFF-479A-BA78-4F042BB2CEF0");
  private static final BluetoothGattService bluetoothGattService1 =
      new BluetoothGattService(TEST_UUID_1, BluetoothGattService.SERVICE_TYPE_PRIMARY);
  private static final BluetoothGattService bluetoothGattService2 =
      new BluetoothGattService(TEST_UUID_2, BluetoothGattService.SERVICE_TYPE_PRIMARY);
  private static final BluetoothGattService bluetoothGattService3 =
      new BluetoothGattService(TEST_UUID_3, BluetoothGattService.SERVICE_TYPE_PRIMARY);

  private BluetoothGattServer bluetoothGattServer;

  @Before
  public void setUp() {
    bluetoothGattServer = ShadowBluetoothGattServer.newInstance();
  }

  @Test
  public void checkBluetoothGattServerIsNotNull() {
    assertNotNull(bluetoothGattServer);
  }

  @Test
  public void addAndGetSingleService() {
    bluetoothGattServer.addService(bluetoothGattService1);
    BluetoothGattService service = bluetoothGattServer.getService(TEST_UUID_1);
    List<BluetoothGattService> services = bluetoothGattServer.getServices();

    assertThat(service).isEqualTo(bluetoothGattService1);
    assertThat(services).contains(bluetoothGattService1);
    assertThat(services).hasSize(1);
  }

  @Test
  public void addAndGetMultipleServices() {
    bluetoothGattServer.addService(bluetoothGattService1);
    bluetoothGattServer.addService(bluetoothGattService2);
    bluetoothGattServer.addService(bluetoothGattService3);
    BluetoothGattService service1 = bluetoothGattServer.getService(TEST_UUID_1);
    BluetoothGattService service2 = bluetoothGattServer.getService(TEST_UUID_2);
    BluetoothGattService service3 = bluetoothGattServer.getService(TEST_UUID_3);
    List<BluetoothGattService> services = bluetoothGattServer.getServices();

    assertThat(service1).isEqualTo(bluetoothGattService1);
    assertThat(service2).isEqualTo(bluetoothGattService2);
    assertThat(service3).isEqualTo(bluetoothGattService3);
    assertThat(services).hasSize(3);
    assertThat(services).contains(bluetoothGattService1);
    assertThat(services).contains(bluetoothGattService2);
    assertThat(services).contains(bluetoothGattService3);
  }

  @Test
  public void getServiceWhenNoneAdded() {
    assertNull(bluetoothGattServer.getService(TEST_UUID_1));
    assertThat(bluetoothGattServer.getServices()).isEmpty();
  }

  @Test
  public void addAndRemoveSingleService() {
    bluetoothGattServer.addService(bluetoothGattService1);
    boolean result = bluetoothGattServer.removeService(bluetoothGattService1);

    assertNull(bluetoothGattServer.getService(TEST_UUID_1));
    assertTrue(result);
    assertThat(bluetoothGattServer.getServices()).isEmpty();
  }

  @Test
  public void addAndRemoveMultipleServices() {
    bluetoothGattServer.addService(bluetoothGattService1);
    bluetoothGattServer.addService(bluetoothGattService2);
    bluetoothGattServer.addService(bluetoothGattService3);
    boolean result1 = bluetoothGattServer.removeService(bluetoothGattService1);
    boolean result2 = bluetoothGattServer.removeService(bluetoothGattService2);
    List<BluetoothGattService> services = bluetoothGattServer.getServices();

    assertNull(bluetoothGattServer.getService(TEST_UUID_1));
    assertNull(bluetoothGattServer.getService(TEST_UUID_2));
    assertThat(bluetoothGattServer.getService(TEST_UUID_3)).isEqualTo(bluetoothGattService3);
    assertTrue(result1);
    assertTrue(result2);
    assertThat(services).hasSize(1);
    assertThat(services).contains(bluetoothGattService3);
  }

  @Test
  public void removeServiceWhenNoneAdded() {
    assertFalse(bluetoothGattServer.removeService(bluetoothGattService1));
  }

  @Test
  public void clearAllServices() {
    bluetoothGattServer.addService(bluetoothGattService1);
    bluetoothGattServer.addService(bluetoothGattService2);
    bluetoothGattServer.addService(bluetoothGattService3);
    bluetoothGattServer.clearServices();

    assertNull(bluetoothGattServer.getService(TEST_UUID_1));
    assertNull(bluetoothGattServer.getService(TEST_UUID_2));
    assertNull(bluetoothGattServer.getService(TEST_UUID_2));
    assertThat(bluetoothGattServer.getServices()).isEmpty();
  }

  @Test
  public void setAndGetGattCallback() {
    BluetoothGattServerCallback callback = new BluetoothGattServerCallback() {};

    shadowOf(bluetoothGattServer).setGattServerCallback(callback);

    assertThat(shadowOf(bluetoothGattServer).getGattServerCallback()).isEqualTo(callback);
  }
}
