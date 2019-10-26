package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.systemContext;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowBluetoothLeScanner}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowBluetoothLeScannerTest {
  private BluetoothLeScanner bluetoothLeScanner;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    bluetoothLeScanner = Shadow.newInstanceOf(BluetoothLeScanner.class);
  }

  @Test
  public void startScanCallback_null_throws() {
    bluetoothLeScanner.startScan(null);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("null callback");
  }

  @Test
  public void startScanCallback_isPending() {
    ScanCallback callback = newScanCallback();

    bluetoothLeScanner.startScan(callback);

    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).containsExactly(callback);
  }

  @Test
  public void startScanCallback_sameTwice_failsAsAlreadyStarted() {
    ScanCallback callback = mock(ScanCallback.class);

    bluetoothLeScanner.startScan(callback);
    bluetoothLeScanner.startScan(callback);

    verify(callback).onScanFailed(ScanCallback.SCAN_FAILED_ALREADY_STARTED);
  }

  @Test
  public void startScanCallbackMultiArgs_null_throws() {
    bluetoothLeScanner.startScan(
        /* filters = */ null, /* scanSettings= */ null, /* callback = */ (ScanCallback) null);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("null callback");
  }

  @Test
  public void startScanCallbackMultiArgs_isPending() {
    ScanCallback callback = newScanCallback();

    bluetoothLeScanner.startScan(
        /* filters = */ null, /* scanSettings= */ null, /* callback = */ callback);

    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).containsExactly(callback);
  }

  @Test
  public void startScanCallbackMultiArgs_sameTwice_failsAsAlreadyStarted() {
    ScanCallback callback = mock(ScanCallback.class);

    bluetoothLeScanner.startScan(/* filters = */ null, /* scanSettings = */ null, callback);
    bluetoothLeScanner.startScan(/* filters = */ null, /* scanSettings= */ null, callback);

    verify(callback).onScanFailed(ScanCallback.SCAN_FAILED_ALREADY_STARTED);
  }

  @Test
  public void startScanCallbackIntent_null_throws() {
    bluetoothLeScanner.startScan(
        /* filters = */ null,
        /* scanSettings= */ null,
        /* callbackIntent = */ (PendingIntent) null);

    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("null callback");
  }

  @Test
  public void startScanCallbackIntent_wellFormed_returnsNoError() {
    int requestCode = 108;
    Intent intent = new Intent("action");
    PendingIntent callbackIntent = PendingIntent.getActivity(systemContext, requestCode, intent, 0);

    int result =
        bluetoothLeScanner.startScan(
            /* filters = */ null, /* scanSettings = */ null, callbackIntent);

    assertThat(result).isEqualTo(0);
    assertThat(shadowOf(bluetoothLeScanner).getCallbackIntents()).containsExactly(callbackIntent);
  }

  @Test
  public void startScanCallbackIntent_sameCallbackIntentTwice_returnsAlreadyStarted() {
    int requestCode = 108;
    Intent intent = new Intent("action");
    PendingIntent callbackIntent = PendingIntent.getActivity(systemContext, requestCode, intent, 0);
    bluetoothLeScanner.startScan(/* filters = */ null, /* scanSettings= */ null, callbackIntent);

    int result =
        bluetoothLeScanner.startScan(
            /* filters = */ null, /* scanSettings = */ null, callbackIntent);

    assertThat(result).isEqualTo(ScanCallback.SCAN_FAILED_ALREADY_STARTED);
  }

  @Test
  public void stopScanCallback_forPendingScan_removesCallback() {
    ScanCallback callback = newScanCallback();
    bluetoothLeScanner.startScan(callback);

    bluetoothLeScanner.stopScan(callback);

    assertThat(shadowOf(bluetoothLeScanner).getScanCallbacks()).doesNotContain(callback);
  }

  @Test
  public void stopScanCallbackIntent_forPendingScan_removesCallbackIntent() {
    int requestCode = 108;
    Intent intent = new Intent("action");
    PendingIntent callbackIntent = PendingIntent.getActivity(systemContext, requestCode, intent, 0);
    bluetoothLeScanner.startScan(/* filters = */ null, /* scanSettings= */ null, callbackIntent);

    bluetoothLeScanner.stopScan(callbackIntent);

    assertThat(shadowOf(bluetoothLeScanner).getCallbackIntents()).doesNotContain(callbackIntent);
  }

  @Test
  public void flushPendingScanResults_beforeStartScan_isNotFlushed() {
    ScanCallback callback = newScanCallback();

    bluetoothLeScanner.flushPendingScanResults(callback);

    assertThat(shadowOf(bluetoothLeScanner).getFlushedPendingScanResults())
        .doesNotContain(callback);
  }

  @Test
  public void flushPendingScanResults_afterStartScan_isFlushed() {
    ScanCallback callback = newScanCallback();
    bluetoothLeScanner.startScan(callback);

    bluetoothLeScanner.flushPendingScanResults(callback);

    assertThat(shadowOf(bluetoothLeScanner).getFlushedPendingScanResults())
        .containsExactly(callback);
  }

  private static ScanCallback newScanCallback() {
    return new ScanCallback() {
      @Override
      public void onBatchScanResults(List<ScanResult> results) {}

      @Override
      public void onScanFailed(int errorCode) {}

      @Override
      public void onScanResult(int callbackType, ScanResult result) {}
    };
  }
}
