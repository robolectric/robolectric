package org.robolectric.shadows;

import static android.companion.virtual.VirtualDeviceManager.LAUNCH_SUCCESS;
import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.PendingIntent;
import android.companion.virtual.IVirtualDevice;
import android.companion.virtual.IVirtualDeviceManager;
import android.companion.virtual.VirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.camera.VirtualCamera;
import android.companion.virtual.camera.VirtualCameraConfig;
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
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/** Shadow for VirtualDeviceManager. */
@Implements(value = VirtualDeviceManager.class, minSdk = UPSIDE_DOWN_CAKE, isInAndroidSdk = false)
public class ShadowVirtualDeviceManager {

  private static final List<VirtualDeviceManager.VirtualDevice> mVirtualDevices = new ArrayList<>();
  private Context context;
  private static IVirtualDeviceManager service;
  @RealObject VirtualDeviceManager realObject;

  @Implementation
  protected void __constructor__(IVirtualDeviceManager service, Context context) {
    this.context = context;
    ShadowVirtualDeviceManager.service = service;
    reflector(VirtualDeviceManagerReflector.class, realObject).__constructor__(service, context);
  }

  @SuppressWarnings("ProtectedImplementationLintCheck")
  @Implementation
  public VirtualDeviceManager.VirtualDevice createVirtualDevice(
      int associationId,
      @ClassName("android.companion.virtual.VirtualDeviceParams") Object params) {
    VirtualDeviceManager.VirtualDevice device =
        ReflectionHelpers.callConstructor(
            VirtualDeviceManager.VirtualDevice.class,
            ClassParameter.from(IVirtualDeviceManager.class, service),
            ClassParameter.from(Context.class, context),
            ClassParameter.from(int.class, associationId),
            ClassParameter.from(VirtualDeviceParams.class, params));
    mVirtualDevices.add(device);
    maybeNotifyVirtualDeviceListeners(context, device.getDeviceId(), /* isClosing= */ false);
    return device;
  }

  @Implementation(minSdk = VANILLA_ICE_CREAM)
  @Nullable
  protected VirtualDevice getVirtualDevice(int deviceId) {
    return getVirtualDevices().stream()
        .filter(virtualDevice -> virtualDevice.getDeviceId() == deviceId)
        .findFirst()
        .orElse(null);
  }

  @Implementation
  @SuppressWarnings("ReturnValueIgnored")
  protected List<VirtualDevice> getVirtualDevices() {
    return mVirtualDevices.stream()
        .map(
            virtualDevice -> {
              VirtualDeviceReflector accessor = reflector(VirtualDeviceReflector.class);
              String deviceName =
                  ((ShadowVirtualDevice) Shadow.extract(virtualDevice)).getParams().getName();
              int[] displayIds =
                  ((ShadowVirtualDevice) Shadow.extract(virtualDevice)).getDisplayIds();
              try {
                // check if VirtualDevice has the U and before constructor
                VirtualDevice.class.getDeclaredConstructor(int.class, String.class);
                return accessor.newInstance(virtualDevice.getDeviceId(), deviceName);
              } catch (NoSuchMethodException e) {
                // Use the new constructor when the old constructor does not exist
                DeviceManagerVirtualDeviceReflector virtualDeviceReflector =
                    reflector(DeviceManagerVirtualDeviceReflector.class, virtualDevice);
                return accessor.newInstanceV(
                    ReflectionHelpers.createDelegatingProxy(
                        IVirtualDevice.class, (VirtualDeviceDelegate) () -> displayIds),
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
      minSdk = UPSIDE_DOWN_CAKE,
      isInAndroidSdk = false)
  public static class ShadowVirtualDevice {
    private static final AtomicInteger nextDeviceId = new AtomicInteger(1);
    private static final AtomicInteger nextCameraId = new AtomicInteger(1);
    static final List<VirtualCamera> virtualCameras = new ArrayList<>();

    @RealObject VirtualDeviceManager.VirtualDevice realVirtualDevice;
    private VirtualDeviceParams params;
    private int deviceId;
    private String persistentDeviceId;
    private PendingIntent pendingIntent;
    private Integer pendingIntentResultCode = LAUNCH_SUCCESS;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private Context context;
    private int associationId;
    private final List<Integer> displayIds = new ArrayList<>();

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

    @Implementation(minSdk = VANILLA_ICE_CREAM)
    @Nullable
    protected String getPersistentDeviceId() {
      return persistentDeviceId;
    }

    @SuppressWarnings("ProtectedImplementationLintCheck")
    @Implementation
    public void close() {
      isClosed.set(true);
      mVirtualDevices.remove(realVirtualDevice);
      maybeNotifyVirtualDeviceListeners(context, deviceId, /* isClosing= */ true);
    }

    public int[] getDisplayIds() {
      return displayIds.stream().mapToInt(i -> (int) i).toArray();
    }

    public boolean isClosed() {
      return isClosed.get();
    }

    VirtualDeviceParams getParams() {
      return params;
    }

    public List<VirtualCamera> getVirtualCameras() {
      return virtualCameras;
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
      if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else if (RuntimeEnvironment.getApiLevel() <= BAKLAVA) {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstancePostB(
            config,
            ReflectionHelpers.createNullProxy(
                loadClass("android.hardware.input.IVirtualInputDevice")));
      }
    }

    @Implementation(minSdk = VANILLA_ICE_CREAM)
    protected @ClassName("android.companion.virtual.camera.VirtualCamera") Object
        createVirtualCamera(
            @ClassName("android.companion.virtual.camera.VirtualCameraConfig") Object config) {
      String id = String.valueOf(nextCameraId.getAndIncrement());

      IVirtualDevice virtualDevice = ReflectionHelpers.createNullProxy(IVirtualDevice.class);
      VirtualCamera virtualCamera =
          new VirtualCamera(virtualDevice, id, (VirtualCameraConfig) config);
      virtualCameras.add(virtualCamera);
      return virtualCamera;
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
      if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else if (RuntimeEnvironment.getApiLevel() <= BAKLAVA) {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstancePostB(
            config,
            ReflectionHelpers.createNullProxy(
                loadClass("android.hardware.input.IVirtualInputDevice")));
      }
    }

    @Implementation
    protected VirtualKeyboard createVirtualKeyboard(@Nonnull VirtualKeyboardConfig config) {
      IBinder token =
          new Binder("android.hardware.input.VirtualKeyboard:" + config.getInputDeviceName());
      VirtualKeyboardReflector accessor = reflector(VirtualKeyboardReflector.class);
      if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
        return accessor.newInstance(ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else if (RuntimeEnvironment.getApiLevel() <= BAKLAVA) {
        return accessor.newInstanceV(
            config, ReflectionHelpers.createNullProxy(IVirtualDevice.class), token);
      } else {
        return accessor.newInstancePostB(
            config,
            ReflectionHelpers.createNullProxy(
                loadClass("android.hardware.input.IVirtualInputDevice")));
      }
    }

    private static Class<?> loadClass(String className) {
      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @SuppressWarnings("ProtectedImplementationLintCheck")
    @Implementation
    public VirtualDisplay createVirtualDisplay(
        @Nonnull VirtualDisplayConfig config,
        @Nullable Executor executor,
        @Nullable VirtualDisplay.Callback callback) {
      VirtualDisplay display =
          DisplayManagerGlobal.getInstance()
              .createVirtualDisplay(context, null, config, callback, executor);
      displayIds.add(display.getDisplay().getDisplayId());
      return display;
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
      nextCameraId.set(1);
      mVirtualDevices.clear();
      virtualCameras.clear();
      service = null;
    }
  }

  private static void maybeNotifyVirtualDeviceListeners(
      Context context, int deviceId, boolean isClosing) {
    if (RuntimeEnvironment.getApiLevel() > UPSIDE_DOWN_CAKE) {
      VirtualDeviceManager vdm = context.getSystemService(VirtualDeviceManager.class);
      List<?> listeners =
          reflector(VirtualDeviceManagerReflector.class, vdm).getVirtualDeviceListeners();
      for (Object listener : listeners) {
        if (isClosing) {
          reflector(VirtualDeviceListenerDelegateReflector.class, listener)
              .onVirtualDeviceClosed(deviceId);
        } else {
          reflector(VirtualDeviceListenerDelegateReflector.class, listener)
              .onVirtualDeviceCreated(deviceId);
        }
      }
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
    VirtualMouse newInstancePostB(
        VirtualMouseConfig config,
        @WithType("android.hardware.input.IVirtualInputDevice") Object virtualDevice);

    @Constructor
    VirtualMouse newInstance(IVirtualDevice virtualDevice, IBinder token);
  }

  @ForType(VirtualTouchscreen.class)
  private interface VirtualTouchscreenReflector {
    @Constructor
    VirtualTouchscreen newInstanceV(
        VirtualTouchscreenConfig config, IVirtualDevice virtualDevice, IBinder token);

    @Constructor
    VirtualTouchscreen newInstancePostB(
        VirtualTouchscreenConfig config,
        @WithType("android.hardware.input.IVirtualInputDevice") Object virtualDevice);

    @Constructor
    VirtualTouchscreen newInstance(IVirtualDevice virtualDevice, IBinder token);
  }

  @ForType(VirtualKeyboard.class)
  private interface VirtualKeyboardReflector {
    @Constructor
    VirtualKeyboard newInstanceV(
        VirtualKeyboardConfig config, IVirtualDevice virtualDevice, IBinder token);

    @Constructor
    VirtualKeyboard newInstancePostB(
        VirtualKeyboardConfig config,
        @WithType("android.hardware.input.IVirtualInputDevice") Object virtualDevice);

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

  @ForType(VirtualDeviceManager.class)
  private interface VirtualDeviceManagerReflector {

    @Direct
    void __constructor__(IVirtualDeviceManager service, Context context);

    @Accessor("mVirtualDeviceListeners")
    List<?> getVirtualDeviceListeners();
  }

  @ForType(
      className = "android.companion.virtual.VirtualDeviceManager$VirtualDeviceListenerDelegate")
  private interface VirtualDeviceListenerDelegateReflector {

    void onVirtualDeviceCreated(int deviceId);

    void onVirtualDeviceClosed(int deviceId);
  }

  private interface VirtualDeviceDelegate {
    int[] getDisplayIds();
  }
}
