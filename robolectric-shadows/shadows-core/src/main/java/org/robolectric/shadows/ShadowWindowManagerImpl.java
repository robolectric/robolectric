package org.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerImpl;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.view.WindowManagerImpl}.
 */
@Implements(value = WindowManagerImpl.class, isInAndroidSdk = false)
public class ShadowWindowManagerImpl extends ShadowWindowManager {
  public static final String WINDOW_MANAGER_IMPL_CLASS_NAME = "android.view.WindowManagerImpl";

  @RealObject Object realObject;
  private List<View> views = new ArrayList<>();

  @Implementation
  public void addView(View view, android.view.ViewGroup.LayoutParams layoutParams) {
    views.add(view);
    directlyOn(realObject, WINDOW_MANAGER_IMPL_CLASS_NAME, "addView",
        ClassParameter.from(View.class, view),
        ClassParameter.from(ViewGroup.LayoutParams.class, layoutParams));
  }

  @Implementation
  public void removeView(View view) {
    views.remove(view);
    directlyOn(realObject, WINDOW_MANAGER_IMPL_CLASS_NAME, "removeView", ClassParameter.from(View.class, view));
  }

  public List<View> getViews() {
    return views;
  }
}
