package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.ParcelUuid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBluetoothLeAdvertiser}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowBluetoothLeAdvertiserTest {

  private static final String ADVERTISE_DATA_UUID1 = "00000000-0000-0000-0000-0000000000A1";
  private static final String ADVERTISE_DATA_UUID2 = "00000000-0000-0000-0000-0000000000A2";
  private static final String SCAN_RESPONSE_UUID1 = "00000000-0000-0000-0000-0000000000B1";
  private static final String SCAN_RESPONSE_UUID2 = "00000000-0000-0000-0000-0000000000B2";
  private static final String CALLBACK1_SUCCESS_RESULT = "c1s";
  private static final String CALLBACK1_FAILURE_RESULT = "c1f";
  private static final String CALLBACK2_SUCCESS_RESULT = "c2s";
  private static final String CALLBACK2_FAILURE_RESULT = "c2f";

  private BluetoothLeAdvertiser bluetoothLeAdvertiser;
  private BluetoothLeAdvertiser bluetoothLeAdvertiserNameSet;
  private AdvertiseSettings advertiseSettings1;
  private AdvertiseSettings advertiseSettings2;
  private AdvertiseData advertiseData1;
  private AdvertiseData advertiseData2;
  private AdvertiseData scanResponse1;
  private AdvertiseData scanResponse2;
  private AdvertiseCallback advertiseCallback1;
  private AdvertiseCallback advertiseCallback2;

  private String result;
  private int error;
  private AdvertiseSettings settings;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    shadowOf(adapter).setState(BluetoothAdapter.STATE_ON);
    bluetoothLeAdvertiser = adapter.getBluetoothLeAdvertiser();

    BluetoothAdapter adapter2 = BluetoothAdapter.getDefaultAdapter();
    adapter2.setName("B");
    bluetoothLeAdvertiserNameSet = adapter.getBluetoothLeAdvertiser();

    advertiseCallback1 =
        new AdvertiseCallback() {
          @Override
          public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            result = CALLBACK1_SUCCESS_RESULT;
            settings = settingsInEffect;
          }

          @Override
          public void onStartFailure(int errorCode) {
            result = CALLBACK1_FAILURE_RESULT;
            error = errorCode;
          }
        };
    advertiseCallback2 =
        new AdvertiseCallback() {
          @Override
          public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            result = CALLBACK2_SUCCESS_RESULT;
            settings = settingsInEffect;
          }

          @Override
          public void onStartFailure(int errorCode) {
            result = CALLBACK2_FAILURE_RESULT;
            error = errorCode;
          }
        };

    advertiseData1 =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString(ADVERTISE_DATA_UUID1))
            .build();
    advertiseData2 =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString(ADVERTISE_DATA_UUID2))
            .build();

    scanResponse1 =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString(SCAN_RESPONSE_UUID1))
            .build();
    scanResponse2 =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString(SCAN_RESPONSE_UUID2))
            .build();

    advertiseSettings1 =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .build();
    advertiseSettings2 =
        new AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .setConnectable(false)
            .build();
  }

  @Test
  public void startAdvertising_nullCallback() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            shadowOf(bluetoothLeAdvertiser)
                .startAdvertising(advertiseSettings1, advertiseData1, null));
  }

  @Test
  public void stopAdvertising_nullCallback() {
    assertThrows(
        IllegalArgumentException.class,
        () -> shadowOf(bluetoothLeAdvertiser).stopAdvertising(null));
  }

  @Test
  public void getAdvertisementRequests_neverStartedAdvertising() {
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void stopAdvertising_neverStartedAdvertising() {
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_oneAdvertisement_withNoScanResponse() {
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings1, advertiseData1, advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
  }

  @Test
  public void startAdvertising_oneAdvertisement_withScanResponse() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
    assertThat(settings).isEqualTo(advertiseSettings1);
  }

  @Test
  public void startAdvertising_twoAdvertisements() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, advertiseData2, advertiseCallback2);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(2);
    assertThat(result).isEqualTo(CALLBACK2_SUCCESS_RESULT);
    assertThat(settings).isEqualTo(advertiseSettings2);
  }

  @Test
  public void startAdvertising_twoAdvertisements_sameCallback() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, advertiseData2, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void startAdvertising_noDataAndoverSizedData_failure() {
    AdvertiseData oversizedData =
        new AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .addServiceUuid(ParcelUuid.fromString("EEEEEEEE-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, null, oversizedData, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, oversizedData, null, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
  }

  @Test
  public void startAdvertising_validSizeUsing16BitUuids() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("00001602-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001604-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001606-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001608-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001610-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001612-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001614-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001616-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001618-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001620-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001622-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001624-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001626-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001628-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, data, advertiseCallback1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void startAdvertising_validSizedUsing32BitUuids() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003208-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003212-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003216-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003220-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003224-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003228-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, data, advertiseCallback1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void startAdvertising_validSizeUsing128BitUuids() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString("F0012816-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings1, data, advertiseCallback1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void startAdvertising_oversizedUsing16BitUuids() {
    AdvertiseData oversizedData =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("00001602-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001604-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001606-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001608-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001610-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001612-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001614-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001616-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001618-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001620-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001622-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001624-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001626-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001628-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001630-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, oversizedData, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_oversizedUsing32BitUuids() {
    AdvertiseData oversizedData =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003208-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003212-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003216-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003220-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003224-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003228-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003232-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings2, oversizedData, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_oversizedWithConnectable() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003208-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003212-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003216-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003220-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003224-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003228-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings1, data, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_oversizedUsing128BitUuids() {
    AdvertiseData oversizedData =
        new AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString("F0012816-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .addServiceUuid(ParcelUuid.fromString("F0012832-FFFF-FFFF-FFFF-FFFFFFFFFFFF"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings1, oversizedData, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_16BitUuids_oversizedDueToConnectable() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("00001602-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001604-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001606-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001608-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001610-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001612-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001614-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001616-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001618-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001620-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001622-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001624-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001626-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("00001628-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiser.startAdvertising(advertiseSettings1, data, advertiseCallback1);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void startAdvertising_validSizeWithNameSet() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiserNameSet.startAdvertising(advertiseSettings2, data, advertiseCallback1);
    assertThat(result).isEqualTo(CALLBACK1_SUCCESS_RESULT);
    assertThat(shadowOf(bluetoothLeAdvertiserNameSet).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void startAdvertising_oversizedWithNameSet() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003208-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003212-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003216-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003220-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003224-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003228-0000-1000-8000-00805F9B34FB"))
            .build();
    bluetoothLeAdvertiserNameSet.startAdvertising(advertiseSettings2, data, advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiserNameSet).getAdvertisementRequestCount()).isEqualTo(0);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
  }

  @Test
  public void startAdvertising_oversizedWithServiceData() {
    AdvertiseData data =
        new AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false)
            .addServiceUuid(ParcelUuid.fromString("F0003204-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003208-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003212-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003216-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003220-0000-1000-8000-00805F9B34FB"))
            .addServiceUuid(ParcelUuid.fromString("F0003224-0000-1000-8000-00805F9B34FB"))
            .addServiceData(
                ParcelUuid.fromString("F0012832-FFFF-FFFF-FFFF-FFFFFFFFFFFF"), new byte[] {1, 2, 3})
            .build();
    bluetoothLeAdvertiserNameSet.startAdvertising(advertiseSettings2, data, advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiserNameSet).getAdvertisementRequestCount()).isEqualTo(0);
    assertThat(error).isEqualTo(AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE);
  }

  @Test
  public void stopAdvertising_afterStartingAdvertising() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(0);
  }

  @Test
  public void stopAdvertising_afterStartingAdvertisingTwice_stoppingFirst() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings2, advertiseData2, scanResponse2, advertiseCallback2);
    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback1);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void stopAdvertising_afterStartingAdvertisingTwice_stoppingSecond() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings2, advertiseData2, scanResponse2, advertiseCallback2);
    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback2);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }

  @Test
  public void stopAdvertising_afterStartingAdvertising_stoppingAdvertisementWasntStarted() {
    bluetoothLeAdvertiser.startAdvertising(
        advertiseSettings1, advertiseData1, scanResponse1, advertiseCallback1);
    bluetoothLeAdvertiser.stopAdvertising(advertiseCallback2);
    assertThat(shadowOf(bluetoothLeAdvertiser).getAdvertisementRequestCount()).isEqualTo(1);
  }
}
