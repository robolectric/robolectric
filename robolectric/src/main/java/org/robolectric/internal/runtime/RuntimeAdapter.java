package org.robolectric.internal.runtime;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;

/**
 * Interface between Robolectric runtime and shadows-core.
 */
public interface RuntimeAdapter {
  void callActivityAttach(Object component, Context baseContext, Class<?> activityThreadClass, Application application, Intent intent, ActivityInfo activityInfo, String activityTitle, Class<?> nonConfigurationInstancesClass);

  void callViewRootImplDispatchResized(Object component, Rect frame, Rect overscanInsets,
      Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
      Configuration newConfig);
}
