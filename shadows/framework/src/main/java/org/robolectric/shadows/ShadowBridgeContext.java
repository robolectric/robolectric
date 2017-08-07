package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityManager;
import android.view.textservice.TextServicesManager;
import com.android.ide.common.rendering.api.AssetRepository;
import com.android.ide.common.rendering.api.IProjectCallback;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.android.BridgePowerManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for {@link com.android.layoutlib.bridge.android.BridgeContext}
 */
@Implements(className = ShadowBridgeContext.CLASS_NAME)
public class ShadowBridgeContext{
  public static final String CLASS_NAME = "com.android.layoutlib.bridge.android.BridgeContext";

  @RealObject private BridgeContext realContext;

  @Implementation
  public Object getSystemService(String service) {
    if (Context.LAYOUT_INFLATER_SERVICE.equals(service)) {
      return ReflectionHelpers.getField(realContext, "mBridgeInflater");
    }
    if (Context.TEXT_SERVICES_MANAGER_SERVICE.equals(service)) {
      return TextServicesManager.getInstance();
    }
    if (Context.WINDOW_SERVICE.equals(service)) {
      return ReflectionHelpers.getField(realContext, "mWindowManager");
    }
    if (Context.INPUT_METHOD_SERVICE.equals(service)) {
      return null;
    }
    if (Context.POWER_SERVICE.equals(service)) {
      return new PowerManager(realContext, new BridgePowerManager(), new Handler());
    }
    if (Context.DISPLAY_SERVICE.equals(service)) {
      return ReflectionHelpers.getField(realContext, "mDisplayManager");
    }
    if (Context.ACCESSIBILITY_SERVICE.equals(service)) {
      return AccessibilityManager.getInstance(realContext);
    }
    if (Context.USER_SERVICE.equals(service)) {
      try {
        Class<?> clazz = Class.forName("android.os.UserManager");
        return newInstanceOf(clazz);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
    throw new UnsupportedOperationException("Unsupported Service: " + service);
  }
  public static BridgeContext obtain(Object projectKey, DisplayMetrics metrics,
      RenderResources renderResources, AssetRepository assets, IProjectCallback projectCallback,
      Configuration config, int targetSdkVersion, boolean hasRtlSupport) {
    return ReflectionHelpers.callConstructor(BridgeContext.class, 
        ClassParameter.from(Object.class, projectKey),
        ClassParameter.from(DisplayMetrics.class, metrics),
        ClassParameter.from(RenderResources.class, renderResources),
//#end
//#if ($api >= 22)
        ClassParameter.from(AssetRepository.class, assets),
//#end
//#if ($api >= 21)
        ClassParameter.from(IProjectCallback.class, projectCallback),
        ClassParameter.from(Configuration.class, config),
        ClassParameter.from(int.class, targetSdkVersion),
        ClassParameter.from(boolean.class, hasRtlSupport));
  }
}
