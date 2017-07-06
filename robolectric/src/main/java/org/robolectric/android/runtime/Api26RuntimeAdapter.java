package org.robolectric.android.runtime;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.ViewRootImpl;
import android.util.MergedConfiguration;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

class Api26RuntimeAdapter implements RuntimeAdapter {

  @Override
  public void callViewRootImplDispatchResized(Object component, Rect frame, Rect overscanInsets,
      Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
      Configuration newConfig) {
    ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
        ClassParameter.from(Rect.class, frame),
        ClassParameter.from(Rect.class, overscanInsets),
        ClassParameter.from(Rect.class, contentInsets),
        ClassParameter.from(Rect.class, visibleInsets),
        ClassParameter.from(Rect.class, stableInsets),
        ClassParameter.from(Rect.class, outsets),
        ClassParameter.from(boolean.class, reportDraw),
        ClassParameter.from(MergedConfiguration.class, null),
        ClassParameter.from(Rect.class, frame),
        ClassParameter.from(boolean.class, false),
        ClassParameter.from(boolean.class, false),
        ClassParameter.from(int.class, 0));
  }
}
