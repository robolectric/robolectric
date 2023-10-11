package org.robolectric.shadows;

import static android.companion.virtual.VirtualDeviceManager.LAUNCH_SUCCESS;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.app.PendingIntent;
import android.companion.virtual.IVirtualDeviceManager;
import android.companion.virtual.VirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.sensor.VirtualSensor;
import android.companion.virtual.sensor.VirtualSensorCallback;
import android.companion.virtual.sensor.VirtualSensorDirectChannelCallback;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for VirtualDeviceManager. */
@Implements(
    value = VirtualDeviceManager.class,
    minSdk = U.SDK_INT,

    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowVirtualDeviceManager {

  private final List<VirtualDeviceManager.VirtualDevice> mVirtualDevices = new ArrayList<>();
  private Context context;
  private IVirtualDeviceManager service;

  @Implementation
  protected void __constructor__(IVirtualDeviceManager service, Context context) {
    this.context = context;
    this.service = service;
  }

  @Implementation
  protected VirtualDeviceManager.VirtualDevice createVirtualDevice(
      int associationId, VirtualDeviceParams params) {
    VirtualDeviceManager.VirtualDevice device =
        ReflectionHelpers.callConstructor(
            VirtualDeviceManager.VirtualDevice.class,
            ClassParameter.from(IVirtualDeviceManager.class, service),
            ClassParameter.from(Context.class, context),
            ClassParameter.from(int.class, associationId),
            ClassParameter.from(VirtualDeviceParams.class, params));
    mVirtualDevices.add(device);
    return device;
  }

  @Implementation
  protected List<android.companion.virtual.VirtualDevice> getVirtualDevices() {
    return mVirtualDevices.stream()
        .map(
            virtualDevice ->
                new VirtualDevice(
                    virtualDevice.getDeviceId(),
                    ((ShadowVirtualDevice) Shadow.extract(virtualDevice)).getParams().getName()))
        .collect(Collectors.toList());
  }

  @Implementation
  protected int getDevicePolicy(int deviceId, int policyType) {
    return mVirtualDevices.stream()
        .filter(virtualDevice -> virtualDevice.getDeviceId() == deviceId)
        .findFirst()
        .map(
            virtualDevice ->
                ((ShadowVirtualDevice) Shadow.extract(virtualDevice))
                    .getParams()
                    .getDevicePolicy(policyType))
        .orElse(VirtualDeviceParams.DEVICE_POLICY_DEFAULT);
  }

  /** Shadow for inner class VirtualDeviceManager.VirtualDevice. */
  @Implements(
      value = VirtualDeviceManager.VirtualDevice.class,
      minSdk = U.SDK_INT,
      // TODO: remove when minimum supported compileSdk is >= 34
      isInAndroidSdk = false)
  public static class ShadowVirtualDevice {
    private static final AtomicInteger nextDeviceId = new AtomicInteger(1);

    @RealObject VirtualDeviceManager.VirtualDevice realVirtualDevice;
    private VirtualDeviceParams params;
    private int deviceId;
    private PendingIntent pendingIntent;
    private Integer pendingIntentResultCode = LAUNCH_SUCCESS;

    @Implementation
    protected void __constructor__(
        IVirtualDeviceManager service,
        Context context,
        int associationId,
        VirtualDeviceParams params) {
      Shadow.invokeConstructor(
          VirtualDeviceManager.VirtualDevice.class,
          realVirtualDevice,
          ClassParameter.from(IVirtualDeviceManager.class, service),
          ClassParameter.from(Context.class, context),
          ClassParameter.from(int.class, associationId),
          ClassParameter.from(VirtualDeviceParams.class, params));
      this.params = params;
      this.deviceId = nextDeviceId.getAndIncrement();
    }

    @Implementation
    protected int getDeviceId() {
      return deviceId;
    }

    /** Prevents a NPE when calling .close() on a VirtualDevice in unit tests. */
    @Implementation
    protected void close() {}

    VirtualDeviceParams getParams() {
      return params;
    }

    @Implementation
    protected List<VirtualSensor> getVirtualSensorList() {
      if (params.getVirtualSensorConfigs() == null) {
        return new ArrayList<>();
      }

      return params.getVirtualSensorConfigs().stream()
          .map(
              config -> {
                VirtualSensor sensor =
                    new VirtualSensor(
                        config.hashCode(), config.getType(), config.getName(), null, null);
                ShadowVirtualSensor shadowSensor = Shadow.extract(sensor);
                shadowSensor.setDeviceId(deviceId);
                return sensor;
              })
          .collect(Collectors.toList());
    }

    @Implementation
    protected void launchPendingIntent(
        int displayId,
        @NonNull PendingIntent pendingIntent,
        @NonNull Executor executor,
        @NonNull IntConsumer listener) {
      this.pendingIntent = pendingIntent;
      executor.execute(() -> listener.accept(pendingIntentResultCode));
    }

    public void setPendingIntentCallbackResultCode(int resultCode) {
      this.pendingIntentResultCode = resultCode;
    }

    public PendingIntent getLastLaunchedPendingIntent() {
      return pendingIntent;
    }

    public VirtualSensorCallback getVirtualSensorCallback() {
      return params.getVirtualSensorCallback() == null
          ? null
          : reflector(
                  VirtualSensorCallbackDelegateReflector.class, params.getVirtualSensorCallback())
              .getCallback();
    }

    public VirtualSensorDirectChannelCallback getVirtualSensorDirectChannelCallback() {
      return params.getVirtualSensorCallback() == null
          ? null
          : reflector(
                  VirtualSensorCallbackDelegateReflector.class, params.getVirtualSensorCallback())
              .getDirectChannelCallback();
    }

    @Resetter
    public static void reset() {
      nextDeviceId.set(1);
    }
  }

  @ForType(
      className =
          "android.companion.virtual.VirtualDeviceParams$Builder$VirtualSensorCallbackDelegate")
  private interface VirtualSensorCallbackDelegateReflector {
    @Accessor("mCallback")
    VirtualSensorCallback getCallback();

    @Accessor("mDirectChannelCallback")
    VirtualSensorDirectChannelCallback getDirectChannelCallback();
  }
}
