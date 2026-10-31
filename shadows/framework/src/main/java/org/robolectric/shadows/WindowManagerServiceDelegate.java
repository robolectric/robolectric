package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.annotation.ClassName;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * The parts of {@link android.view.IWindowManager} that need an answer. Other calls return the
 * default value for their type.
 */
@SuppressWarnings("unused") // Called through a proxy.
class WindowManagerServiceDelegate implements ShadowServiceManager.ResettableService {
  /** The display of each window context that got a display's configuration, by token. */
  private static final Map<IBinder, Integer> windowContextDisplayIds =
      Collections.synchronizedMap(new WeakHashMap<>());

  /** Sends the display's new configuration to its window contexts. */
  static void onDisplayChanged(int displayId) {
    Configuration configuration = WindowConfigurations.getDisplayOverrideConfiguration(displayId);
    if (configuration == null) {
      return;
    }
    List<IBinder> clientTokens = new ArrayList<>();
    synchronized (windowContextDisplayIds) {
      windowContextDisplayIds.forEach(
          (clientToken, id) -> {
            if (id == displayId) {
              clientTokens.add(clientToken);
            }
          });
    }
    for (IBinder clientToken : clientTokens) {
      sendConfiguration(clientToken, configuration, displayId);
    }
  }

  private static void sendConfiguration(
      IBinder clientToken, Configuration configuration, int displayId) {
    ReflectionHelpers.callInstanceMethod(
        clientToken,
        "onConfigurationChanged",
        ClassParameter.from(Configuration.class, configuration),
        ClassParameter.from(int.class, displayId));
  }

  @Override
  public void reset() {
    windowContextDisplayIds.clear();
  }

  public IBinder asBinder() {
    return ServiceManager.getService(Context.WINDOW_SERVICE);
  }

  /**
   * Gives a window context on a non-default display that display's configuration. On S this returns
   * whether it was attached and sends the configuration to the token; from S_V2 it returns the
   * configuration.
   */
  @Nullable
  public Object attachWindowContextToDisplayArea(
      IBinder clientToken, int type, int displayId, Bundle options) {
    Configuration configuration = WindowConfigurations.getDisplayOverrideConfiguration(displayId);
    if (configuration != null) {
      windowContextDisplayIds.put(clientToken, displayId);
    }
    if (RuntimeEnvironment.getApiLevel() == S) {
      if (configuration == null) {
        return false;
      }
      sendConfiguration(clientToken, configuration, displayId);
      return true;
    }
    return configuration;
  }

  /** As above, for VANILLA_ICE_CREAM and later. */
  @Nullable
  public Object attachWindowContextToDisplayArea(
      @ClassName("android.app.IApplicationThread") Object appThread,
      IBinder clientToken,
      int type,
      int displayId,
      Bundle options) {
    Configuration configuration = WindowConfigurations.getDisplayOverrideConfiguration(displayId);
    if (configuration == null) {
      return null;
    }
    windowContextDisplayIds.put(clientToken, displayId);
    return ReflectionHelpers.callConstructor(
        ReflectionHelpers.loadClass(
            WindowManagerServiceDelegate.class.getClassLoader(),
            "android.window.WindowContextInfo"),
        ClassParameter.from(Configuration.class, configuration),
        ClassParameter.from(int.class, displayId));
  }
}
