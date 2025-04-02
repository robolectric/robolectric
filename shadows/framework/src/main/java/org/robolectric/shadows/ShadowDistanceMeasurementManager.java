package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.IBluetoothGatt;
import android.bluetooth.le.DistanceMeasurementManager;
import android.bluetooth.le.DistanceMeasurementMethod;
import android.bluetooth.le.DistanceMeasurementParams;
import android.bluetooth.le.DistanceMeasurementResult;
import android.bluetooth.le.DistanceMeasurementSession;
import android.content.AttributionSource;
import android.os.CancellationSignal;
import android.os.ParcelUuid;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow implementation of {@link DistanceMeasurementManager}. */
@Implements(
    value = DistanceMeasurementManager.class,
    minSdk = UPSIDE_DOWN_CAKE,
    isInAndroidSdk = false)
public class ShadowDistanceMeasurementManager {

  private final Map<BluetoothDevice, DistanceMeasurementSession> sessionMap = new HashMap<>();
  private final Map<BluetoothDevice, DistanceMeasurementSession.Callback> sessionCallbackMap =
      new HashMap<>();
  private List<DistanceMeasurementMethod> supportedMethods = new ArrayList<>();

  @Implementation
  protected List<DistanceMeasurementMethod> getSupportedMethods() {
    return supportedMethods;
  }

  @Implementation
  protected CancellationSignal startMeasurementSession(
      DistanceMeasurementParams params,
      Executor executor,
      DistanceMeasurementSession.Callback callback) {
    Class<?> iDistanceMeasurementClazz = null;
    try {
      iDistanceMeasurementClazz =
          ReflectionHelpers.loadClass(
              Thread.currentThread().getContextClassLoader(),
              "android.bluetooth.IDistanceMeasurement");
    } catch (RuntimeException e) {
      // no op, class not available
    }
    if (iDistanceMeasurementClazz != null
        && ReflectionHelpers.hasConstructor(
            DistanceMeasurementSession.class,
            iDistanceMeasurementClazz,
            ParcelUuid.class,
            DistanceMeasurementParams.class,
            Executor.class,
            AttributionSource.class,
            DistanceMeasurementSession.Callback.class)) {
      sessionMap.put(
          params.getDevice(),
          ReflectionHelpers.callConstructor(
              DistanceMeasurementSession.class,
              ClassParameter.from(
                  iDistanceMeasurementClazz,
                  ReflectionHelpers.createNullProxy(iDistanceMeasurementClazz)),
              ClassParameter.from(ParcelUuid.class, new ParcelUuid(UUID.randomUUID())),
              ClassParameter.from(DistanceMeasurementParams.class, params),
              ClassParameter.from(Executor.class, executor),
              ClassParameter.from(AttributionSource.class, AttributionSource.myAttributionSource()),
              ClassParameter.from(DistanceMeasurementSession.Callback.class, callback)));
    } else {
      sessionMap.put(
          params.getDevice(),
          ReflectionHelpers.callConstructor(
              DistanceMeasurementSession.class,
              ClassParameter.from(
                  IBluetoothGatt.class, ReflectionHelpers.createNullProxy(IBluetoothGatt.class)),
              ClassParameter.from(ParcelUuid.class, new ParcelUuid(UUID.randomUUID())),
              ClassParameter.from(DistanceMeasurementParams.class, params),
              ClassParameter.from(Executor.class, executor),
              ClassParameter.from(AttributionSource.class, AttributionSource.myAttributionSource()),
              ClassParameter.from(DistanceMeasurementSession.Callback.class, callback)));
    }
    sessionCallbackMap.put(params.getDevice(), callback);

    return new CancellationSignal();
  }

  /**
   * Simulates {@link DistanceMeasurementSession.Callback#onResult(BluetoothDevice,
   * DistanceMeasurementResult)}.
   *
   * @param device Remote {@link BluetoothDevice} to which this device is measuring distance.
   * @param result {@link DistanceMeasurementResult} which should be passed to the callback.
   */
  public void simulateOnResult(BluetoothDevice device, DistanceMeasurementResult result) {
    DistanceMeasurementSession session = sessionMap.get(device);
    DistanceMeasurementSession.Callback sessionCallback = sessionCallbackMap.get(device);
    if (session == null || sessionCallback == null) {
      throw new NoSuchElementException("Session or session callback is missing.");
    }
    sessionCallback.onStarted(session);
    sessionCallback.onResult(device, result);
  }

  /**
   * Simulates {@link DistanceMeasurementSession.Callback#onStartFail(int)} with an error.
   *
   * @param device Remote {@link BluetoothDevice} to which this device is measuring distance.
   * @param error Error to simulate. One of {@link DistanceMeasurementSession.Callback.Reason}.
   */
  public void simulateOnStartFailError(BluetoothDevice device, int error) {
    DistanceMeasurementSession.Callback sessionCallback = sessionCallbackMap.get(device);
    if (sessionCallback == null) {
      throw new NoSuchElementException("Session callback is missing.");
    }
    sessionCallback.onStartFail(error);

    sessionMap.remove(device);
    sessionCallbackMap.remove(device);
  }

  /**
   * Simulates {@link DistanceMeasurementSession.Callback#onStopped(DistanceMeasurementSession,
   * int)} with an error.
   *
   * @param device Remote {@link BluetoothDevice} to which this device is measuring distance.
   * @param error Error to simulate. One of {@link DistanceMeasurementSession.Callback.Reason}.
   */
  public void simulateOnStoppedError(BluetoothDevice device, int error) {
    DistanceMeasurementSession session = sessionMap.get(device);
    DistanceMeasurementSession.Callback sessionCallback = sessionCallbackMap.get(device);
    if (session == null || sessionCallback == null) {
      throw new NoSuchElementException("Session or session callback is missing.");
    }
    sessionCallback.onStarted(session);
    sessionCallback.onStopped(session, error);

    sessionMap.remove(device);
    sessionCallbackMap.remove(device);
  }

  /**
   * Simulates {@link DistanceMeasurementSession.Callback#onStopped(DistanceMeasurementSession,
   * int)} without an error.
   *
   * @param device Remote {@link BluetoothDevice} to which this device is measuring distance.
   */
  public void simulateSuccessfulTermination(BluetoothDevice device) {
    DistanceMeasurementSession session = sessionMap.get(device);
    DistanceMeasurementSession.Callback sessionCallback = sessionCallbackMap.get(device);
    if (session == null || sessionCallback == null) {
      throw new NoSuchElementException("Session or session callback is missing.");
    }
    sessionCallback.onStarted(session);
    sessionCallback.onStopped(session, BluetoothStatusCodes.REASON_LOCAL_APP_REQUEST);

    sessionMap.remove(device);
    sessionCallbackMap.remove(device);
  }

  /**
   * Simulates {@link DistanceMeasurementSession.Callback#onStopped(DistanceMeasurementSession,
   * int)} after a timeout.
   */
  public void simulateTimeout(BluetoothDevice device) {
    DistanceMeasurementSession session = sessionMap.get(device);
    DistanceMeasurementSession.Callback sessionCallback = sessionCallbackMap.get(device);
    if (session == null || sessionCallback == null) {
      throw new NoSuchElementException("Session or session callback is missing.");
    }
    sessionCallback.onStarted(session);
    sessionCallback.onStopped(session, BluetoothStatusCodes.ERROR_TIMEOUT);

    sessionMap.remove(device);
    sessionCallbackMap.remove(device);
  }

  /** Sets a list of supported {@link DistanceMeasurementMethod}. */
  public void setSupportedMethods(List<DistanceMeasurementMethod> methods) {
    supportedMethods = ImmutableList.copyOf(methods);
  }
}
