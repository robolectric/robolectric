package org.robolectric.android.runtime;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.view.ViewRootImpl;

import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

class Api16RuntimeAdapter implements RuntimeAdapter{

  @Override
  public void callViewRootImplDispatchResized(Object component, Rect frame, Rect overscanInsets,
      Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
      Configuration newConfig) {
    ReflectionHelpers.callInstanceMethod(ViewRootImpl.class, component, "dispatchResized",
        ClassParameter.from(int.class, frame.width()),
        ClassParameter.from(int.class, frame.height()),
        ClassParameter.from(Rect.class, contentInsets),
        ClassParameter.from(Rect.class, visibleInsets),
        ClassParameter.from(boolean.class, reportDraw),
        ClassParameter.from(Configuration.class, newConfig));
  }
}

