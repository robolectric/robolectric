package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBluetoothLeAdvertiser}. */
@Config(minSdk = O)
@RunWith(RobolectricTestRunner.class)
public class ShadowBluetoothLeAdvertiserTest {
  private BluetoothLeAdvertiser bluetoothLeAdvertiser;
  private ParcelUuid serviceUuid;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    bluetoothLeAdvertiser = adapter.getBluetoothLeAdvertiser();
    serviceUuid = new ParcelUuid(UUID.fromString("12345678-90AB-CDEF-1234-567890ABCDEF"));
  }

  @Test
  public void startAdvertising_advertiseSuccessful() throws Exception {
    TestAdvertiseCallback testAdvertiseCallback = new TestAdvertiseCallback();
    AdvertiseSettings advertiseSettings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
    AdvertiseData data = new AdvertiseData.Builder().addServiceUuid(serviceUuid).build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, testAdvertiseCallback);
    shadowMainLooper().idle();
    assertThat(testAdvertiseCallback.advertiseSuccessful).isTrue();
    assertThat(testAdvertiseCallback.advertiseFailed).isFalse();
  }

  @Test
  public void startAdvertising_advertiseDataTooLarge() throws Exception {
    TestAdvertiseCallback testAdvertiseCallback = new TestAdvertiseCallback();
    AdvertiseSettings advertiseSettings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
    byte[] serviceData = "VeryLargeServiceData".getBytes(UTF_8);
    AdvertiseData data =
        new AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .addServiceData(serviceUuid, serviceData)
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, testAdvertiseCallback);
    shadowMainLooper().idle();
    assertThat(testAdvertiseCallback.advertiseSuccessful).isFalse();
    assertThat(testAdvertiseCallback.advertiseFailed).isTrue();
  }

  @Test
  public void startAdvertising_advertisingAlreadyStarted() throws Exception {
    TestAdvertiseCallback testAdvertiseCallback = new TestAdvertiseCallback();
    AdvertiseSettings advertiseSettings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
    AdvertiseData data = new AdvertiseData.Builder().addServiceUuid(serviceUuid).build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, testAdvertiseCallback);
    shadowMainLooper().idle();
    assertThat(testAdvertiseCallback.advertiseSuccessful).isTrue();
    assertThat(testAdvertiseCallback.advertiseFailed).isFalse();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, testAdvertiseCallback);
    shadowMainLooper().idle();
    assertThat(testAdvertiseCallback.advertiseFailed).isTrue();
  }

  @Test
  public void startAdvertisingSet_advertiseSuccessful() throws Exception {
    TestAdvertisingSetCallback testAdvertisingSetCallback = new TestAdvertisingSetCallback();
    AdvertiseData data = new AdvertiseData.Builder().addServiceUuid(serviceUuid).build();
    AdvertisingSetParameters parameters =
        new AdvertisingSetParameters.Builder()
            .setConnectable(false)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_LOW)
            .setInterval(400)
            .build();
    bluetoothLeAdvertiser.startAdvertisingSet(
        parameters, data, null, null, null, testAdvertisingSetCallback);
    assertThat(testAdvertisingSetCallback.advertiseSuccessful).isTrue();
  }

  @Test
  public void startAdvertisingSet_advertiseDataTooLarge() throws Exception {
    TestAdvertisingSetCallback testAdvertisingSetCallback = new TestAdvertisingSetCallback();
    byte[] serviceData = "VeryLargeServiceData".getBytes(UTF_8);
    AdvertiseData data =
        new AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .addServiceData(serviceUuid, serviceData)
            .build();
    AdvertisingSetParameters parameters =
        new AdvertisingSetParameters.Builder()
            .setConnectable(false)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_LOW)
            .setInterval(400)
            .build();
    bluetoothLeAdvertiser.startAdvertisingSet(
        parameters, data, null, null, null, testAdvertisingSetCallback);
    assertThat(testAdvertisingSetCallback.advertiseSuccessful).isFalse();
    assertThat(testAdvertisingSetCallback.advertiseDataTooLarge).isTrue();
  }

  @Test
  public void stopAdvertisingSet() throws Exception {
    TestAdvertisingSetCallback testAdvertisingSetCallback = new TestAdvertisingSetCallback();
    AdvertiseData data = new AdvertiseData.Builder().addServiceUuid(serviceUuid).build();
    AdvertisingSetParameters parameters =
        new AdvertisingSetParameters.Builder()
            .setConnectable(false)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_LOW)
            .setInterval(400)
            .build();
    bluetoothLeAdvertiser.startAdvertisingSet(
        parameters, data, null, null, null, testAdvertisingSetCallback);
    assertThat(testAdvertisingSetCallback.advertiseSuccessful).isTrue();
    bluetoothLeAdvertiser.stopAdvertisingSet(testAdvertisingSetCallback);
    assertThat(testAdvertisingSetCallback.advertiseStopped).isTrue();
  }

  private static class TestAdvertiseCallback extends AdvertiseCallback {
    boolean advertiseSuccessful;
    boolean advertiseFailed;

    @Override
    public void onStartSuccess(AdvertiseSettings settings) {
      advertiseSuccessful = true;
    }

    @Override
    public void onStartFailure(int errorCode) {
      advertiseFailed = true;
    }
  }

  private static class TestAdvertisingSetCallback extends AdvertisingSetCallback {
    boolean advertiseDataTooLarge;
    boolean advertiseSuccessful;
    boolean advertiseStopped;

    @Override
    public void onAdvertisingSetStarted(AdvertisingSet set, int txPower, int status) {
      if (status == AdvertisingSetCallback.ADVERTISE_SUCCESS) {
        advertiseSuccessful = true;
      } else if (status == AdvertisingSetCallback.ADVERTISE_FAILED_DATA_TOO_LARGE) {
        advertiseDataTooLarge = true;
      }
    }

    @Override
    public void onAdvertisingSetStopped(AdvertisingSet set) {
      advertiseStopped = true;
    }
  }
}
