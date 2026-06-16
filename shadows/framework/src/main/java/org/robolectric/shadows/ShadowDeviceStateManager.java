package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.CallbackExecutor;
import android.hardware.devicestate.DeviceState;
import android.hardware.devicestate.DeviceState.DeviceStateProperties;
import android.hardware.devicestate.DeviceStateManager;
import android.hardware.devicestate.DeviceStateManager.DeviceStateCallback;
import android.hardware.devicestate.DeviceStateManagerGlobal;
import android.util.ArraySet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Filter.Order;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** A Shadow for [android.hardware.devicestate.DeviceStateManager] added in Android V. */
@Implements(value = DeviceStateManager.class, minSdk = VANILLA_ICE_CREAM, isInAndroidSdk = false)
public final class ShadowDeviceStateManager {

  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static final List<DeviceStateCallback> callbacks = new ArrayList<>();

  private static final Set<Integer> properties = new ArraySet<>();

  public void addProperty(@DeviceStateProperties int property) {
    properties.add(property);
    updateSystemProperties();
  }

  public void removeProperty(@DeviceStateProperties int property) {
    properties.remove(property);
    updateSystemProperties();
  }

  private void updateSystemProperties() {
    DeviceState.Configuration configuration =
        new DeviceState.Configuration.Builder(0, "TestDevice")
            .setPhysicalProperties(properties)
            .build();
    DeviceState state = new DeviceState(configuration);
    synchronized (lock) {
      for (DeviceStateCallback callback : callbacks) {
        callback.onDeviceStateChanged(state);
      }
    }
  }

  @Filter(order = Order.AFTER)
  protected void registerCallback(
      @Nonnull @CallbackExecutor Executor executor, @Nonnull DeviceStateCallback callback) {
    synchronized (lock) {
      callbacks.add(callback);
    }
  }

  @Filter(order = Order.AFTER)
  protected void unregisterCallback(@Nonnull DeviceStateCallback callback) {
    synchronized (lock) {
      callbacks.remove(callback);
    }
  }

  @Resetter
  public static void reset() {
    reflector(DeviceStateManagerGlobalReflector.class).setInstance(null);
    synchronized (lock) {
      callbacks.clear();
    }
    properties.clear();
  }

  @ForType(DeviceStateManagerGlobal.class)
  interface DeviceStateManagerGlobalReflector {
    @Static
    @Accessor("sInstance")
    void setInstance(DeviceStateManagerGlobal instance);
  }
}
