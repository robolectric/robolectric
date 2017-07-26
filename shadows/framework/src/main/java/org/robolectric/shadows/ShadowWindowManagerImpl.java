package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerImpl;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
