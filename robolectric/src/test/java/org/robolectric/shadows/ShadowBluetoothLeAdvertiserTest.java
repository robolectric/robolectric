package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBluetoothLeAdvertiser}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class ShadowBluetoothLeAdvertiserTest {
  private BluetoothLeAdvertiser bluetoothLeAdvertiser;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    bluetoothLeAdvertiser = adapter.getBluetoothLeAdvertiser();
  }

  @Test
  public void startAdvertising() throws Exception {
    AdvertiseSettings advertiseSettings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
    AdvertiseData data = new AdvertiseData.Builder().build();

    CountDownLatch latch = new CountDownLatch(1);
    AdvertiseCallback callback =
        new AdvertiseCallback() {
          @Override
          public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            latch.countDown();
          }

          @Override
          public void onStartFailure(int errorCode) {}
        };

    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, callback);
    latch.await();
  }

  @Test
  public void startAdvertising_withScanResponse() throws Exception {
    AdvertiseSettings advertiseSettings =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build();
    AdvertiseData data = new AdvertiseData.Builder().build();
    AdvertiseData scanResponse = new AdvertiseData.Builder().build();

    CountDownLatch latch = new CountDownLatch(1);
    AdvertiseCallback callback =
        new AdvertiseCallback() {
          @Override
          public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            latch.countDown();
          }

          @Override
          public void onStartFailure(int errorCode) {}
        };

    bluetoothLeAdvertiser.startAdvertising(advertiseSettings, data, scanResponse, callback);
    latch.await();
  }
}
