package org.robolectric.android.runtime;

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

  void callViewRootImplDispatchResized(Object component, Rect frame, Rect overscanInsets,
      Rect contentInsets, Rect visibleInsets, Rect stableInsets, Rect outsets, boolean reportDraw,
      Configuration newConfig);
}
