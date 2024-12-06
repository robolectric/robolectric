package org.robolectric.shadows;

import static android.companion.virtual.VirtualDeviceManager.LAUNCH_SUCCESS;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.PendingIntent;
import android.companion.virtual.IVirtualDevice;
import android.companion.virtual.IVirtualDeviceManager;
import android.companion.virtual.VirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.sensor.VirtualSensor;
import android.companion.virtual.sensor.VirtualSensorCallback;
import android.companion.virtual.sensor.VirtualSensorDirectChannelCallback;
import android.content.Context;
import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplayConfig;
import android.hardware.input.VirtualKeyboard;
import android.hardware.input.VirtualKeyboardConfig;
import android.hardware.input.VirtualMouse;
import android.hardware.input.VirtualMouseConfig;
import android.hardware.input.VirtualTouchscreen;
import android.hardware.input.VirtualTouchscreenConfig;
import android.os.Binder;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for VirtualDeviceManager. */
@Implements(value = VirtualDeviceManager.class, minSdk = U.SDK_INT, isInAndroidSdk = false)
public class ShadowVirtualDeviceManager {

  private static final List<VirtualDeviceManager.VirtualDevice> mVirtualDevices = new ArrayList<>();
  private Context context;
  private static IVirtualDeviceManager service;

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
  @SuppressWarnings("ReturnValueIgnored")
  protected List<android.companion.virtual.VirtualDevice> getVirtualDevices() {
    return mVirtualDevices.stream()
        .map(
            virtualDevice -> {
              VirtualDeviceReflector accessor = reflector(VirtualDeviceReflector.class);
              String deviceName =
                  ((ShadowVirtualDevice) Shadow.extract(virtualDevice)).getParams().getName();
              try {
                // check if VirtualDevice has the U and before constructor
                VirtualDevice.class.getDeclaredConstructor(int.class, String.class);
                return accessor.newInstance(virtualDevice.getDeviceId(), deviceName);
              } catch (NoSuchMethodException e) {
                // Use the new constructor when the old constructor does not exist
                DeviceManagerVirtualDeviceReflector virtualDeviceReflector =
                    reflector(DeviceManagerVirtualDeviceReflector.class, virtualDevice);
                return accessor.newInstanceV(
                    ReflectionHelpers.createNullProxy(IVirtualDevice.class),
                    virtualDevice.getDeviceId(),
                    virtualDeviceReflector.getPersistentDeviceId(),
                    deviceName);
              }
            })
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

  @Implementation
  protected boolean isValidVirtualDeviceId(int deviceId) {
    return mVirtualDevices.stream()
        .anyMatch(virtualDevice -> virtualDevice.getDeviceId() == deviceId);
  }

  /** Shadow for inner class VirtualDeviceManager.VirtualDevice. */
  @Implements(
      value = VirtualDeviceManager.VirtualDevice.class,
      minSdk = U.SDK_INT,
      isInAndroidSdk = false)
  public static class ShadowVirtualDevice {
    private static final AtomicInteger nextDeviceId = new AtomicInteger(1);

    @RealObject VirtualDeviceManager.VirtualDevice realVirtualDevice;
    private VirtualDeviceParams params;
    private int deviceId;
    private String persistentDeviceId;
    private PendingIntent pendingIntent;
    private Integer pendingIntentResultCode = LAUNCH_SUCCESS;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private Context context;
    private int associationId;

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
      this.context = context;
      this.associationId = associationId;
      this.persistentDeviceId = "companion:" + associationId;
    }

    @Implementation
    protected int getDeviceId() {
      return deviceId;
    }

    @Implementation
    protected Context createContext() {
      return context.createDeviceContext(deviceId);
    }

    @Implementation(minSdk = V.SDK_INT)
    @Nullable
    protected String getPersistentDeviceId() {
      return persistentDeviceId;
    }

    /** Prevents a NPE when calling .close() on a VirtualDevice in unit tests. */
    @Implementation
    protected void close() {
      isClosed.set(true);
    }

    public boolean isClosed() {
      return isClosed.get();
    }

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
        @Nonnull PendingIntent pendingIntent,
        @Nonnull Executor executor,
        @Nonnull IntConsumer listener) {
      this.pendingIntent = pendingIntent;
      executor.execute(() -> listener.accept(pendingIntentResultCode));
    }

    @Implementation
    protected VirtualMouse createVirtualMouse(
        @Nonnull VirtualDisplay display,
        @Nonnull String inputDeviceName,
        int vendorId,
        int productId) {
      return createVirtualMouse(
          new VirtualMouseConfig.Builder().setInputDeviceName(inputDeviceName).build());
    }

    @Implementation
    protected VirtualMouse createVirtualMouse(@Nonnull VirtualMouseConfig config) {
      IBinder token =
          new Binder("android.hardware.input.VirtualMouse:" + config.getInputDeviceName());
      VirtualMouseReflector accessor = reflector(VirtualMouseReflector.class);
      if (RuntimeEnvironment.getApiLevel() <= U.SDK_INT) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      }
    }

    @Implementation
    protected void setShowPointerIcon(boolean showPointerIcon) {
      // no-op
    }

    @Implementation
    protected VirtualTouchscreen createVirtualTouchscreen(
        @Nonnull VirtualDisplay display,
        @Nonnull String inputDeviceName,
        int vendorId,
        int productId) {
      int displayWidth = 720;
      int displayHeight = 1280;
      return createVirtualTouchscreen(
          new VirtualTouchscreenConfig.Builder(displayWidth, displayHeight)
              .setInputDeviceName(inputDeviceName)
              .build());
    }

    @Implementation
    protected VirtualTouchscreen createVirtualTouchscreen(
        @Nonnull VirtualTouchscreenConfig config) {
      IBinder token =
          new Binder("android.hardware.input.VirtualTouchscreen:" + config.getInputDeviceName());
      VirtualTouchscreenReflector accessor = reflector(VirtualTouchscreenReflector.class);
      if (RuntimeEnvironment.getApiLevel() <= U.SDK_INT) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      }
    }

    @Implementation
    protected VirtualKeyboard createVirtualKeyboard(@Nonnull VirtualKeyboardConfig config) {
      IBinder token =
          new Binder("android.hardware.input.VirtualKeyboard:" + config.getInputDeviceName());
      VirtualKeyboardReflector accessor = reflector(VirtualKeyboardReflector.class);
      if (RuntimeEnvironment.getApiLevel() <= U.SDK_INT) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      }
    }

    @Implementation
    protected VirtualDisplay createVirtualDisplay(
        @Nonnull VirtualDisplayConfig config,
        @Nullable Executor executor,
        @Nullable VirtualDisplay.Callback callback) {
      return DisplayManagerGlobal.getInstance()
          .createVirtualDisplay(context, null, config, callback, executor);
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
      mVirtualDevices.clear();
      service = null;
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

  @ForType(VirtualMouse.class)
  private interface VirtualMouseReflector {
    @Constructor
    VirtualMouse newInstanceV(
        VirtualMouseConfig config, IVirtualDevice virtualDevice, IBinder token);

    @Constructor
    VirtualMouse newInstance(IVirtualDevice virtualDevice, IBinder token);
  }

  @ForType(VirtualTouchscreen.class)
  private interface VirtualTouchscreenReflector {
    @Constructor
    VirtualTouchscreen newInstanceV(
        VirtualTouchscreenConfig config, IVirtualDevice virtualDevice, IBinder token);

    @Constructor
    VirtualTouchscreen newInstance(IVirtualDevice virtualDevice, IBinder token);
  }

  @ForType(VirtualKeyboard.class)
  private interface VirtualKeyboardReflector {
    @Constructor
    VirtualKeyboard newInstanceV(
        VirtualKeyboardConfig config, IVirtualDevice virtualDevice, IBinder token);

    @Constructor
    VirtualKeyboard newInstance(IVirtualDevice virtualDevice, IBinder token);
  }

  @ForType(VirtualDevice.class)
  private interface VirtualDeviceReflector {
    @Constructor
    VirtualDevice newInstanceV(
        IVirtualDevice virtualDevice, int id, String persistentId, String name);

    @Constructor
    VirtualDevice newInstance(int id, String name);
  }

  @ForType(VirtualDeviceManager.VirtualDevice.class)
  private interface DeviceManagerVirtualDeviceReflector {
    String getPersistentDeviceId();
  }
}
