package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.ParcelUuid;
import androidx.test.core.app.ApplicationProvider;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowBluetoothLeScanner}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = LOLLIPOP)
public class ShadowBluetoothLeScannerTest {
  private BluetoothLeScanner bluetoothLeScanner;
  private List<ScanFilter> scanFilters;
  private ScanSettings scanSettings;
  private ScanCallback scanCallback;
  private PendingIntent pendingIntent;

  @Before
  public void setUp() throws Exception {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (RuntimeEnvironment.getApiLevel() < M) {
      // On SDK < 23, bluetooth has to be in STATE_ON in order to get a BluetoothLeScanner.
      shadowOf(adapter).setState(BluetoothAdapter.STATE_ON);
    }
    bluetoothLeScanner = adapter.getBluetoothLeScanner();

    ParcelUuid serviceUuid =
        new ParcelUuid(UUID.fromString("12345678-90AB-CDEF-1234-567890ABCDEF"));
    byte[] serviceData = new byte[] {0x01, 0x02, 0x03};

    scanFilters =
        Collections.singletonList(
            new ScanFilter.Builder()
                .setServiceUuid(serviceUuid)
                .setServiceData(serviceUuid, serviceData)
                .build());
    scanSettings =
        new ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setReportDelay(0)
            .build();
    scanCallback =
        new ScanCallback() {
          @Override
          public void onScanResult(int callbackType, ScanResult scanResult) {}

          @Override
          public void onScanFailed(int errorCode) {}
        };
    pendingIntent =
        PendingIntent.getBroadcast(
            ApplicationProvider.getApplicationContext(), 0, new Intent("SCAN_CALLBACK"), 0);
  }

  @Test
  public void startScanning() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).containsExactly(scanCallback);
  }

  @Test
  public void startScanning_withNullParameters() {
    bluetoothLeScanner.startScan(null, null, scanCallback);
    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).containsExactly(scanCallback);
  }

  @Test
  public void stopScanning() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).containsExactly(scanCallback);
    bluetoothLeScanner.stopScan(scanCallback);
    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).isEmpty();
  }

  @Test
  public void stopScanning_neverStarted() {
    bluetoothLeScanner.stopScan(scanCallback);
  }

  @Test
  @Config(minSdk = O)
  public void startScanning_forPendingIntent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, pendingIntent);
    assertThat(shadowOf(bluetoothLeScanner).getPendingIntents()).containsExactly(pendingIntent);
  }

  @Test
  @Config(minSdk = O)
  public void startScanning_forPendingIntent_withNullParameters() {
    bluetoothLeScanner.startScan(null, null, pendingIntent);
    assertThat(shadowOf(bluetoothLeScanner).getPendingIntents()).containsExactly(pendingIntent);
  }

  @Test
  @Config(minSdk = O)
  public void stopScanning_forPendingIntent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, pendingIntent);
    assertThat(shadowOf(bluetoothLeScanner).getPendingIntents()).containsExactly(pendingIntent);
    bluetoothLeScanner.stopScan(pendingIntent);
    assertThat(shadowOf(bluetoothLeScanner).getPendingIntents()).isEmpty();
  }

  @Test
  public void getScanFilters_forScanCallback_isPresent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    assertThat(
            shadowOf(bluetoothLeScanner)
                .getScanFilters(scanCallback)
                .orElse(Collections.emptyList()))
        .containsExactlyElementsIn(scanFilters);
  }

  @Test
  public void getScanFilters_forScanCallback_isAbsent() {
    assertThat(shadowOf(bluetoothLeScanner).getScanFilters(scanCallback).isPresent()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void getScanFilters_forPendingIntent_isPresent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, pendingIntent);
    assertThat(
            shadowOf(bluetoothLeScanner)
                .getScanFilters(pendingIntent)
                .orElse(Collections.emptyList()))
        .containsExactlyElementsIn(scanFilters);
  }

  @Test
  @Config(minSdk = O)
  public void getScanFilters_forPendingIntent_isAbsent() {
    assertThat(shadowOf(bluetoothLeScanner).getScanFilters(pendingIntent).isPresent()).isFalse();
  }

  @Test
  public void getScanSettings_forScanCallback_isPresent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
    assertThat(shadowOf(bluetoothLeScanner).getScanSettings(scanCallback).orElse(null))
        .isEqualTo(scanSettings);
  }

  @Test
  public void getScanSettings_forScanCallback_isAbsent() {
    assertThat(shadowOf(bluetoothLeScanner).getScanSettings(scanCallback).isPresent()).isFalse();
  }

  @Test
  @Config(minSdk = O)
  public void getScanSettings_forPendingIntent_isPresent() {
    bluetoothLeScanner.startScan(scanFilters, scanSettings, pendingIntent);
    assertThat(shadowOf(bluetoothLeScanner).getScanSettings(pendingIntent).orElse(null))
        .isEqualTo(scanSettings);
  }

  @Test
  @Config(minSdk = O)
  public void getScanSettings_forPendingIntent_isAbsent() {
    assertThat(shadowOf(bluetoothLeScanner).getScanSettings(pendingIntent).isPresent()).isFalse();
  }
}
