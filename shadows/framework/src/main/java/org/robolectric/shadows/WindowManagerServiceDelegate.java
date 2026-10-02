package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ServiceManager;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.internal.WindowConfigurations;
import org.robolectric.annotation.ClassName;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * The parts of {@link android.view.IWindowManager} that need an answer. Every other call returns
 * the default value for its type.
 */
@SuppressWarnings("unused") // Called through a proxy.
class WindowManagerServiceDelegate {

  public IBinder asBinder() {
    return ServiceManager.getService(Context.WINDOW_SERVICE);
  }

  /**
   * Gives a window context on a non-default display that display's configuration. A window context
   * on the default display keeps the global configuration, as before.
   *
   * <p>On S this reports whether the context was attached and sends the configuration to the
   * context's token; from S_V2 it returns the configuration.
   */
  @Nullable
  public Object attachWindowContextToDisplayArea(
      IBinder clientToken, int type, int displayId, Bundle options) {
    Configuration configuration = WindowConfigurations.getDisplayOverrideConfiguration(displayId);
    if (RuntimeEnvironment.getApiLevel() == S) {
      if (configuration == null) {
        return false;
      }
      ReflectionHelpers.callInstanceMethod(
          clientToken,
          "onConfigurationChanged",
          ClassParameter.from(Configuration.class, configuration),
          ClassParameter.from(int.class, displayId));
      return true;
    }
    return configuration;
  }

  /** As above, for VANILLA_ICE_CREAM and later, which return a WindowContextInfo. */
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
    return ReflectionHelpers.callConstructor(
        ReflectionHelpers.loadClass(
            WindowManagerServiceDelegate.class.getClassLoader(),
            "android.window.WindowContextInfo"),
        ClassParameter.from(Configuration.class, configuration),
        ClassParameter.from(int.class, displayId));
  }
}
