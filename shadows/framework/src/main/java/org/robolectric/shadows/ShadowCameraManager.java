package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.impl.CameraDeviceImpl;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.InDevelopment;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;
import org.robolectric.versioning.AndroidVersions.Baklava;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow class for {@link CameraManager} */
@Implements(value = CameraManager.class)
public class ShadowCameraManager {
  // Keep references to cameras so they can be closed after each test
  protected static final Set<CameraDeviceImpl> createdCameras =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
  // LinkedHashMap used to ensure getCameraIdList returns ids in the order in which they were added
  private static final Map<String, CameraCharacteristics> cameraIdToCharacteristics =
      new LinkedHashMap<>();
  private static final Map<String, Boolean> cameraTorches = new HashMap<>();
  private static final Set<CameraManager.AvailabilityCallback> registeredCallbacks =
      new HashSet<>();
  // Cannot reference the torch callback in < Android M
  private static final Set<Object> torchCallbacks = new HashSet<>();
  // Most recent camera device opened with openCamera
  private static CameraDevice lastDevice;
  // Most recent callback passed to openCamera
  private static CameraDevice.StateCallback lastCallback;
  @Nullable private static Executor lastCallbackExecutor;
  @Nullable private static Handler lastCallbackHandler;
  @RealObject private CameraManager realObject;

  @Resetter
  public static void reset() {
    for (CameraDeviceImpl cameraDevice : createdCameras) {
      if (cameraDevice != null) {
        cameraDevice.close();
      }
    }
    createdCameras.clear();
    cameraIdToCharacteristics.clear();
    cameraTorches.clear();
    registeredCallbacks.clear();
    torchCallbacks.clear();
    if (lastDevice != null) {
      lastDevice.close();
    }
    lastDevice = null;
    lastCallback = null;
    lastCallbackExecutor = null;
    if (lastCallbackHandler != null) {
      // Flush existing handler tasks to ensure camera related callbacks are called properly.
      shadowOf(lastCallbackHandler.getLooper()).idle();
      lastCallbackHandler.removeCallbacksAndMessages(null);
    }
    lastCallbackHandler = null;
  }

  @Implementation
  @Nonnull
  protected String[] getCameraIdList() throws CameraAccessException {
    Set<String> cameraIds = cameraIdToCharacteristics.keySet();
    return cameraIds.toArray(new String[0]);
  }

  @Implementation
  @Nonnull
  protected CameraCharacteristics getCameraCharacteristics(@Nonnull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    CameraCharacteristics characteristics = cameraIdToCharacteristics.get(cameraId);
    Preconditions.checkArgument(characteristics != null);
    return characteristics;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void setTorchMode(@Nonnull String cameraId, boolean enabled) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkArgument(cameraIdToCharacteristics.keySet().contains(cameraId));
    cameraTorches.put(cameraId, enabled);
    for (Object callback : torchCallbacks) {
      ((CameraManager.TorchCallback) callback).onTorchModeChanged(cameraId, enabled);
    }
  }

  @Implementation(minSdk = U.SDK_INT, maxSdk = U.SDK_INT)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId,
      CameraDevice.StateCallback callback,
      Executor executor,
      final int uid,
      final int oomScoreOffset,
      boolean overrideToPortrait) {
    return openCameraDeviceUserAsync(cameraId, callback, executor, uid, oomScoreOffset);
  }

  @Implementation(minSdk = V.SDK_INT, maxSdk = V.SDK_INT)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId,
      CameraDevice.StateCallback callback,
      Executor executor,
      final int uid,
      final int oomScoreOffset,
      int rotationOverride) {
    return openCameraDeviceUserAsync(cameraId, callback, executor, uid, oomScoreOffset);
  }

  // in development API has reverted back to the T signature. Just use a different method name
  // to avoid conflicts.
  // TODO: increment this to  minSdk next-SDK-after-V once V is fully released
  @Implementation(methodName = "openCameraDeviceUserAsync", minSdk = Baklava.SDK_INT)
  @InDevelopment
  protected CameraDevice openCameraDeviceUserAsyncPostV(
      String cameraId,
      CameraDevice.StateCallback callback,
      Executor executor,
      int unusedClientUid,
      int unusedOomScoreOffset,
      boolean unused) {
    return openCameraDeviceUserAsync(
        cameraId, callback, executor, unusedClientUid, unusedOomScoreOffset);
  }

  @Implementation(minSdk = Build.VERSION_CODES.S, maxSdk = Build.VERSION_CODES.TIRAMISU)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId,
      CameraDevice.StateCallback callback,
      Executor executor,
      int unusedClientUid,
      int unusedOomScoreOffset) {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
    Context context = RuntimeEnvironment.getApplication();
    CameraDeviceImpl deviceImpl =
        createCameraDeviceImpl(cameraId, callback, executor, characteristics, context);
    createdCameras.add(deviceImpl);
    updateCameraCallback(deviceImpl, callback, null, executor);
    executor.execute(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  @Implementation(minSdk = VERSION_CODES.P, maxSdk = VERSION_CODES.R)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Executor executor, final int uid)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
    Context context = reflector(ReflectorCameraManager.class, realObject).getContext();

    CameraDeviceImpl deviceImpl =
        ReflectionHelpers.callConstructor(
            CameraDeviceImpl.class,
            ClassParameter.from(String.class, cameraId),
            ClassParameter.from(CameraDevice.StateCallback.class, callback),
            ClassParameter.from(Executor.class, executor),
            ClassParameter.from(CameraCharacteristics.class, characteristics),
            ClassParameter.from(int.class, context.getApplicationInfo().targetSdkVersion));

    createdCameras.add(deviceImpl);
    updateCameraCallback(deviceImpl, callback, null, executor);
    executor.execute(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  @Implementation(minSdk = VERSION_CODES.N_MR1, maxSdk = VERSION_CODES.O_MR1)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Handler handler, final int uid)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);
    Context context = reflector(ReflectorCameraManager.class, realObject).getContext();

    CameraDeviceImpl deviceImpl;
    if (Build.VERSION.SDK_INT == VERSION_CODES.N_MR1) {
      deviceImpl =
          ReflectionHelpers.callConstructor(
              CameraDeviceImpl.class,
              ClassParameter.from(String.class, cameraId),
              ClassParameter.from(CameraDevice.StateCallback.class, callback),
              ClassParameter.from(Handler.class, handler),
              ClassParameter.from(CameraCharacteristics.class, characteristics));
    } else {
      deviceImpl =
          ReflectionHelpers.callConstructor(
              CameraDeviceImpl.class,
              ClassParameter.from(String.class, cameraId),
              ClassParameter.from(CameraDevice.StateCallback.class, callback),
              ClassParameter.from(Handler.class, handler),
              ClassParameter.from(CameraCharacteristics.class, characteristics),
              ClassParameter.from(int.class, context.getApplicationInfo().targetSdkVersion));
    }
    createdCameras.add(deviceImpl);
    updateCameraCallback(deviceImpl, callback, handler, null);
    handler.post(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  /**
   * Enables {@link CameraManager#openCamera(String, StateCallback, Handler)} to open a {@link
   * CameraDevice}.
   *
   * <p>If the provided cameraId exists, this will always post {@link
   * CameraDevice.StateCallback#onOpened(CameraDevice)} to the provided {@link Handler}. Unlike on
   * real Android, this will not check if the camera has been disabled by device policy and does not
   * attempt to connect to the camera service, so {@link
   * CameraDevice.StateCallback#onError(CameraDevice, int)} and {@link
   * CameraDevice.StateCallback#onDisconnected(CameraDevice)} will not be triggered by {@link
   * CameraManager#openCamera(String, StateCallback, Handler)}.
   */
  @Implementation(maxSdk = VERSION_CODES.N)
  protected CameraDevice openCameraDeviceUserAsync(
      String cameraId, CameraDevice.StateCallback callback, Handler handler)
      throws CameraAccessException {
    CameraCharacteristics characteristics = getCameraCharacteristics(cameraId);

    CameraDeviceImpl deviceImpl =
        ReflectionHelpers.callConstructor(
            CameraDeviceImpl.class,
            ClassParameter.from(String.class, cameraId),
            ClassParameter.from(CameraDevice.StateCallback.class, callback),
            ClassParameter.from(Handler.class, handler),
            ClassParameter.from(CameraCharacteristics.class, characteristics));

    createdCameras.add(deviceImpl);
    updateCameraCallback(deviceImpl, callback, handler, null);
    handler.post(() -> callback.onOpened(deviceImpl));
    return deviceImpl;
  }

  @Implementation
  protected void registerAvailabilityCallback(
      CameraManager.AvailabilityCallback callback, Handler handler) {
    Preconditions.checkNotNull(callback);
    registeredCallbacks.add(callback);
  }

  @Implementation
  protected void unregisterAvailabilityCallback(CameraManager.AvailabilityCallback callback) {
    Preconditions.checkNotNull(callback);
    registeredCallbacks.remove(callback);
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void registerTorchCallback(CameraManager.TorchCallback callback, Handler handler) {
    Preconditions.checkNotNull(callback);
    torchCallbacks.add(callback);
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected void unregisterTorchCallback(CameraManager.TorchCallback callback) {
    Preconditions.checkNotNull(callback);
    torchCallbacks.remove(callback);
  }

  private CameraDeviceImpl createCameraDeviceImpl(
      String cameraId,
      CameraDevice.StateCallback callback,
      Executor executor,
      CameraCharacteristics characteristics,
      Context context) {
    Map<String, CameraCharacteristics> cameraCharacteristicsMap = Collections.emptyMap();
    if (RuntimeEnvironment.getApiLevel() >= Baklava.SDK_INT) {
      return reflector(ReflectorCameraDeviceImpl.class)
          .newCameraDeviceImplPostV(
              cameraId,
              callback,
              executor,
              characteristics,
              realObject,
              context.getApplicationInfo().targetSdkVersion,
              context,
              null,
              false);

    } else if (RuntimeEnvironment.getApiLevel() == V.SDK_INT) {
      return reflector(ReflectorCameraDeviceImpl.class)
          .newCameraDeviceImplV(
              cameraId,
              callback,
              executor,
              characteristics,
              realObject,
              context.getApplicationInfo().targetSdkVersion,
              context,
              null);
    } else {
      return reflector(ReflectorCameraDeviceImpl.class)
          .newCameraDeviceImpl(
              cameraId,
              callback,
              executor,
              characteristics,
              cameraCharacteristicsMap,
              context.getApplicationInfo().targetSdkVersion,
              context);
    }
  }

  /**
   * Calls all registered callbacks's onCameraAvailable method. This is a no-op if no callbacks are
   * registered.
   */
  private void triggerOnCameraAvailable(@Nonnull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    for (CameraManager.AvailabilityCallback callback : registeredCallbacks) {
      callback.onCameraAvailable(cameraId);
    }
  }

  /**
   * Calls all registered callbacks's onCameraUnavailable method. This is a no-op if no callbacks
   * are registered.
   */
  private void triggerOnCameraUnavailable(@Nonnull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    for (CameraManager.AvailabilityCallback callback : registeredCallbacks) {
      callback.onCameraUnavailable(cameraId);
    }
  }

  /**
   * Adds the given cameraId and characteristics to this shadow.
   *
   * <p>The result from {@link #getCameraIdList()} will be in the order in which cameras were added.
   *
   * @throws IllegalArgumentException if there's already an existing camera with the given id.
   */
  public void addCamera(@Nonnull String cameraId, @Nonnull CameraCharacteristics characteristics) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkNotNull(characteristics);
    Preconditions.checkArgument(!cameraIdToCharacteristics.containsKey(cameraId));

    cameraIdToCharacteristics.put(cameraId, characteristics);
    triggerOnCameraAvailable(cameraId);
  }

  /**
   * Removes the given cameraId and associated characteristics from this shadow.
   *
   * @throws IllegalArgumentException if there is not an existing camera with the given id.
   */
  public void removeCamera(@Nonnull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkArgument(cameraIdToCharacteristics.containsKey(cameraId));

    cameraIdToCharacteristics.remove(cameraId);
    triggerOnCameraUnavailable(cameraId);
  }

  /** Returns what the supplied camera's torch is set to. */
  public boolean getTorchMode(@Nonnull String cameraId) {
    Preconditions.checkNotNull(cameraId);
    Preconditions.checkArgument(cameraIdToCharacteristics.keySet().contains(cameraId));
    Boolean torchState = cameraTorches.get(cameraId);
    return torchState;
  }

  /**
   * Triggers a disconnect event, where any open camera will be disconnected (simulating the case
   * where another app takes control of the camera).
   */
  public void triggerDisconnect() {
    if (lastCallbackHandler != null) {
      lastCallbackHandler.post(() -> lastCallback.onDisconnected(lastDevice));
    } else if (lastCallbackExecutor != null) {
      lastCallbackExecutor.execute(() -> lastCallback.onDisconnected(lastDevice));
    }
  }

  protected void updateCameraCallback(
      CameraDevice device,
      CameraDevice.StateCallback callback,
      @Nullable Handler handler,
      @Nullable Executor executor) {
    lastDevice = device;
    lastCallback = callback;
    lastCallbackHandler = handler;
    lastCallbackExecutor = executor;
  }

  @ForType(CameraDeviceImpl.class)
  interface ReflectorCameraDeviceImpl {
    @Constructor
    CameraDeviceImpl newCameraDeviceImpl(
        String cameraId,
        CameraDevice.StateCallback callback,
        Executor executor,
        CameraCharacteristics characteristics,
        Map<String, CameraCharacteristics> characteristicsMap,
        int targetSdkVersion,
        Context context);

    @Constructor
    CameraDeviceImpl newCameraDeviceImplV(
        String cameraId,
        CameraDevice.StateCallback callback,
        Executor executor,
        CameraCharacteristics characteristics,
        CameraManager cameraManager,
        int targetSdkVersion,
        Context context,
        @WithType("android.hardware.camera2.CameraDevice$CameraDeviceSetup")
            Object cameraDeviceSetup);

    @Constructor
    CameraDeviceImpl newCameraDeviceImplPostV(
        String cameraId,
        CameraDevice.StateCallback callback,
        Executor executor,
        CameraCharacteristics characteristics,
        CameraManager cameraManager,
        int targetSdkVersion,
        Context context,
        @WithType("android.hardware.camera2.CameraDevice$CameraDeviceSetup")
            Object cameraDeviceSetup,
        boolean unused);
  }

  /** Accessor interface for {@link CameraManager}'s internals. */
  @ForType(CameraManager.class)
  private interface ReflectorCameraManager {

    @Accessor("mContext")
    Context getContext();
  }

  /** Shadow class for internal class CameraManager$CameraManagerGlobal */
  @Implements(
      className = "android.hardware.camera2.CameraManager$CameraManagerGlobal",
      minSdk = VERSION_CODES.LOLLIPOP_MR1)
  public static class ShadowCameraManagerGlobal {

    /**
     * Cannot create a CameraService connection within Robolectric. Avoid endless reconnect loop.
     */
    @Implementation(minSdk = VERSION_CODES.N)
    protected void scheduleCameraServiceReconnectionLocked() {}
  }
}
