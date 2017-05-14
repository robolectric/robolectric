package org.robolectric.shadows;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Looper;
import android.view.ViewRootImpl;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import static android.os.Build.VERSION_CODES.*;

@Implements(value = ViewRootImpl.class, isInAndroidSdk = false)
public class ShadowViewRootImpl {

  @RealObject
  private ViewRootImpl realObject;

  @Implementation
  public static Object getWindowSession(Looper mainLooper) {
    return null;
  }

  @Implementation
  public void playSoundEffect(int effectId) {
  }

  public void callViewRootImplDispatchResized(Rect frame, Rect overscanInsets,
                                              Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
                                              Configuration newConfig) {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(int.class, frame.width()),
          ReflectionHelpers.ClassParameter.from(int.class, frame.height()),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig));
    } else if (RuntimeEnvironment.getApiLevel() < JELLY_BEAN_MR1) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig));
    } else if (RuntimeEnvironment.getApiLevel() < KITKAT) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, overscanInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig));
    } else if (RuntimeEnvironment.getApiLevel() < LOLLIPOP_MR1) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, overscanInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, stableInsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig));
    } else if (RuntimeEnvironment.getApiLevel() < M) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, overscanInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, stableInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, outsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig));
    } else if (RuntimeEnvironment.getApiLevel() < N) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, overscanInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, stableInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, outsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig),
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(boolean.class, false),
          ReflectionHelpers.ClassParameter.from(boolean.class, false));
    } else if (RuntimeEnvironment.getApiLevel() < O) {
      ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, realObject, "dispatchResized",
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(Rect.class, overscanInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, contentInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, visibleInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, stableInsets),
          ReflectionHelpers.ClassParameter.from(Rect.class, outsets),
          ReflectionHelpers.ClassParameter.from(boolean.class, reportDraw),
          ReflectionHelpers.ClassParameter.from(Configuration.class, newConfig),
          ReflectionHelpers.ClassParameter.from(Rect.class, frame),
          ReflectionHelpers.ClassParameter.from(boolean.class, false),
          ReflectionHelpers.ClassParameter.from(boolean.class, false),
          ReflectionHelpers.ClassParameter.from(int.class, 0));
    } else {
      throw new RuntimeException("Unable to handle API level: " + RuntimeEnvironment.getApiLevel());
    }
  }


}
