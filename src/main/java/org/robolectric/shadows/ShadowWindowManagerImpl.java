package org.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.List;

import static org.robolectric.Robolectric.directlyOn;

@Implements(value = Robolectric.Anything.class, className = ShadowWindowManagerImpl.WINDOW_MANAGER_IMPL_CLASS_NAME)
public class ShadowWindowManagerImpl extends ShadowWindowManager {
  public static final String WINDOW_MANAGER_IMPL_CLASS_NAME = "android.view.WindowManagerImpl";

  @RealObject Object realObject;
  private List<View> views = new ArrayList<View>();

  @Implementation
  public void addView(View view, android.view.ViewGroup.LayoutParams layoutParams) {
    views.add(view);
    directlyOn(realObject, WINDOW_MANAGER_IMPL_CLASS_NAME, "addView",
        View.class, ViewGroup.LayoutParams.class).invoke(view, layoutParams);
  }

  @Implementation
  public void removeView(View view) {
    views.remove(view);

    directlyOn(realObject, WINDOW_MANAGER_IMPL_CLASS_NAME, "removeView",
        View.class).invoke(view);
  }

  public List<View> getViews() {
    return views;
  }
}
