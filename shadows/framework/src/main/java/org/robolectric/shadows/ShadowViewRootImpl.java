package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Looper;
import android.util.MergedConfiguration;
import android.view.Display;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import java.util.ArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  @RealObject private ViewRootImpl realObject;

  @Implementation(maxSdk = JELLY_BEAN)
  public static Object getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {
  }

  public void callDispatchResized() {
    Display display = getDisplay();
    Rect frame = new Rect();
    display.getRectSize(frame);
    Rect zeroSizedRect = new Rect(0, 0, 0, 0);

    int apiLevel = RuntimeEnvironment.getApiLevel();
    ViewRootImpl component = realObject;
    if (apiLevel <= Build.VERSION_CODES.JELLY_BEAN) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(int.class, frame.width()),
          ClassParameter.from(int.class, frame.height()),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null));
    } else if (apiLevel <= JELLY_BEAN_MR1) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null));
    } else if (apiLevel <= Build.VERSION_CODES.KITKAT) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null));
    } else if (apiLevel <= Build.VERSION_CODES.LOLLIPOP_MR1) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null));
    } else if (apiLevel <= Build.VERSION_CODES.M) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null));
    } else if (apiLevel <= Build.VERSION_CODES.N_MR1) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(Configuration.class, null),
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false));
    } else if (apiLevel <= Build.VERSION_CODES.O_MR1) {
      ReflectionHelpers.callInstanceMethod(
          ViewRootImpl.class,
          component,
          "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(MergedConfiguration.class, new MergedConfiguration()),
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(int.class, 0));

    } else if (apiLevel <= Build.VERSION_CODES.P) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(Rect.class, zeroSizedRect),
          ClassParameter.from(boolean.class, true),
          ClassParameter.from(MergedConfiguration.class, new MergedConfiguration()),
          ClassParameter.from(Rect.class, frame),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(boolean.class, false),
          ClassParameter.from(int.class, 0),
          ClassParameter.from(android.view.DisplayCutout.ParcelableWrapper.class,
              new android.view.DisplayCutout.ParcelableWrapper()));

    } else {
      throw new RuntimeException("Could not find AndroidRuntimeAdapter for API level: " + apiLevel);
    }
  }

  private Display getDisplay() {
    if (RuntimeEnvironment.getApiLevel() > JELLY_BEAN_MR1) {
      return realObject.getView().getDisplay();
    } else {
      WindowManager windowManager = (WindowManager) realObject.getView().getContext()
          .getSystemService(Context.WINDOW_SERVICE);
      return windowManager.getDefaultDisplay();
    }
  }

  @Resetter
  public static void reset() {
     ReflectionHelpers.setStaticField(ViewRootImpl.class, "sRunQueues", new ThreadLocal<>());
     ReflectionHelpers.setStaticField(ViewRootImpl.class, "sFirstDrawHandlers", new ArrayList<>());
     ReflectionHelpers.setStaticField(ViewRootImpl.class, "sFirstDrawComplete", false);
     ReflectionHelpers.setStaticField(ViewRootImpl.class, "sConfigCallbacks", new ArrayList<>());
  }
}
