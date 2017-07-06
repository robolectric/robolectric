package org.robolectric.android.runtime;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.ViewRootImpl;

import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

class Api22RuntimeAdapter implements RuntimeAdapter {

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
        ClassParameter.from(boolean.class, reportDraw),
        ClassParameter.from(Configuration.class, newConfig));
  }
}
