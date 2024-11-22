package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.le.DistanceMeasurementManager;
import android.bluetooth.le.DistanceMeasurementMethod;
import android.bluetooth.le.DistanceMeasurementParams;
import android.bluetooth.le.DistanceMeasurementResult;
import android.bluetooth.le.DistanceMeasurementSession;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowDistanceMeasurementManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = UPSIDE_DOWN_CAKE)
public class ShadowDistanceMeasurementManagerTest {

  private static final String REMOTE_ADDRESS = "11:22:33:AA:BB:CC";

  private int onStartFailReason;
  private int onStoppedReason;

  private final Context context = ApplicationProvider.getApplicationContext();
  private final BluetoothAdapter adapter =
      context.getSystemService(BluetoothManager.class).getAdapter();
  private final BluetoothDevice remoteDevice = adapter.getRemoteDevice(REMOTE_ADDRESS);
  private final Executor executor = MoreExecutors.directExecutor();

  private Object distanceMeasurementManager;
  private ShadowDistanceMeasurementManager shadowDistanceMeasurementManager;

  private final Object params = new DistanceMeasurementParams.Builder(remoteDevice).build();
  private Object startedDistanceMeasurementSession;
  private final List<DistanceMeasurementResult> distanceMeasurementResults = new ArrayList<>();
  private final Object distanceMeasurementSessionCallback =
      new DistanceMeasurementSession.Callback() {
        @Override
        public void onStarted(@Nonnull DistanceMeasurementSession session) {
          startedDistanceMeasurementSession = session;
        }

        @Override
        public void onStartFail(int reason) {
          onStartFailReason = reason;
        }

        @Override
        public void onStopped(@Nonnull DistanceMeasurementSession session, int reason) {
          onStoppedReason = reason;
        }

        @Override
        public void onResult(
            @Nonnull BluetoothDevice device, @Nonnull DistanceMeasurementResult result) {
          distanceMeasurementResults.add(result);
        }
      };

  @Before
  public void setUp() {
    shadowOf(adapter).setDistanceMeasurementSupported(BluetoothStatusCodes.FEATURE_SUPPORTED);
    distanceMeasurementManager = adapter.getDistanceMeasurementManager();
    shadowDistanceMeasurementManager = Shadow.extract(distanceMeasurementManager);
  }

  @Test
  public void getSupportedMethods_whenMethodsSet_returnsSetMethods() {
    DistanceMeasurementMethod methodAuto =
        new DistanceMeasurementMethod.Builder(
                DistanceMeasurementMethod.DISTANCE_MEASUREMENT_METHOD_AUTO)
            .build();
    DistanceMeasurementMethod methodRssi =
        new DistanceMeasurementMethod.Builder(
                DistanceMeasurementMethod.DISTANCE_MEASUREMENT_METHOD_RSSI)
            .build();

    shadowDistanceMeasurementManager.setSupportedMethods(ImmutableList.of(methodAuto, methodRssi));

    assertThat(((DistanceMeasurementManager) distanceMeasurementManager).getSupportedMethods())
        .containsExactly(methodAuto, methodRssi);
  }

  @Test
  public void startMeasurementSession_onSessionSuccess_invokesOnResult() {
    ((DistanceMeasurementManager) distanceMeasurementManager)
        .startMeasurementSession(
            (DistanceMeasurementParams) params,
            executor,
            (DistanceMeasurementSession.Callback) distanceMeasurementSessionCallback);
    shadowDistanceMeasurementManager.simulateOnResult(
        remoteDevice, new DistanceMeasurementResult.Builder(123, 12).build());
    shadowDistanceMeasurementManager.simulateOnResult(
        remoteDevice, new DistanceMeasurementResult.Builder(321, 21).build());
    shadowDistanceMeasurementManager.simulateSuccessfulTermination(remoteDevice);

    assertThat(startedDistanceMeasurementSession).isNotNull();
    assertThat(distanceMeasurementResults.get(0).getResultMeters()).isEqualTo(123);
    assertThat(distanceMeasurementResults.get(0).getErrorMeters()).isEqualTo(12);
    assertThat(distanceMeasurementResults.get(1).getResultMeters()).isEqualTo(321);
    assertThat(distanceMeasurementResults.get(1).getErrorMeters()).isEqualTo(21);
  }

  @Test
  public void startMeasurementSession_onSessionStartFailed_invokesOnStartFail() {
    ((DistanceMeasurementManager) distanceMeasurementManager)
        .startMeasurementSession(
            (DistanceMeasurementParams) params,
            executor,
            (DistanceMeasurementSession.Callback) distanceMeasurementSessionCallback);
    shadowDistanceMeasurementManager.simulateOnStartFailError(
        remoteDevice, BluetoothStatusCodes.ERROR_UNKNOWN);

    assertThat(startedDistanceMeasurementSession).isNull();
    assertThat(onStartFailReason).isEqualTo(BluetoothStatusCodes.ERROR_UNKNOWN);
    assertThat(distanceMeasurementResults).isEmpty();
  }

  @Test
  public void startMeasurementSession_onSessionStopped_invokesOnStopped() {
    ((DistanceMeasurementManager) distanceMeasurementManager)
        .startMeasurementSession(
            (DistanceMeasurementParams) params,
            executor,
            (DistanceMeasurementSession.Callback) distanceMeasurementSessionCallback);
    shadowDistanceMeasurementManager.simulateOnStoppedError(
        remoteDevice, BluetoothStatusCodes.ERROR_UNKNOWN);

    assertThat(startedDistanceMeasurementSession).isNotNull();
    assertThat(onStoppedReason).isEqualTo(BluetoothStatusCodes.ERROR_UNKNOWN);
    assertThat(distanceMeasurementResults).isEmpty();
  }

  @Test
  public void startMeasurementSession_onTimeout_invokeOnStoppedWithTimeoutError() {
    ((DistanceMeasurementManager) distanceMeasurementManager)
        .startMeasurementSession(
            (DistanceMeasurementParams) params,
            executor,
            (DistanceMeasurementSession.Callback) distanceMeasurementSessionCallback);
    shadowDistanceMeasurementManager.simulateTimeout(remoteDevice);

    assertThat(startedDistanceMeasurementSession).isNotNull();
    assertThat(onStoppedReason).isEqualTo(BluetoothStatusCodes.ERROR_TIMEOUT);
    assertThat(distanceMeasurementResults).isEmpty();
  }
}
