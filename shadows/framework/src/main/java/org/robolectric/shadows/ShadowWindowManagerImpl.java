package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.R;
import static android.view.View.SYSTEM_UI_FLAG_VISIBLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.InsetsState;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowManagerImpl;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowViewRootImpl.ViewRootImplReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(value = WindowManagerImpl.class, isInAndroidSdk = false)
public class ShadowWindowManagerImpl extends ShadowWindowManager {

  private static Display defaultDisplayJB;

  @RealObject WindowManagerImpl realObject;
  private static final Multimap<Integer, View> views = ArrayListMultimap.create();

  // removed from WindowManagerImpl in S
  public static final int NEW_INSETS_MODE_FULL = 2;

  /** internal only */
  public static void configureDefaultDisplayForJBOnly(
      Configuration configuration, DisplayMetrics displayMetrics) {
    Class<?> arg2Type =
        ReflectionHelpers.loadClass(
            ShadowWindowManagerImpl.class.getClassLoader(), "android.view.CompatibilityInfoHolder");

    defaultDisplayJB =
        ReflectionHelpers.callConstructor(
            Display.class, ClassParameter.from(int.class, 0), ClassParameter.from(arg2Type, null));
    ShadowDisplay shadowDisplay = Shadow.extract(defaultDisplayJB);
    shadowDisplay.configureForJBOnly(configuration, displayMetrics);
  }

  @Implementation
  public void addView(View view, android.view.ViewGroup.LayoutParams layoutParams) {
    views.put(realObject.getDefaultDisplay().getDisplayId(), view);
    // views.add(view);
    reflector(ReflectorWindowManagerImpl.class, realObject).addView(view, layoutParams);
  }

  @Implementation
  public void removeView(View view) {
    views.remove(realObject.getDefaultDisplay().getDisplayId(), view);
    reflector(ReflectorWindowManagerImpl.class, realObject).removeView(view);
  }

  @Implementation
  protected void removeViewImmediate(View view) {
    views.remove(realObject.getDefaultDisplay().getDisplayId(), view);
    reflector(ReflectorWindowManagerImpl.class, realObject).removeViewImmediate(view);
  }

  public List<View> getViews() {
    return ImmutableList.copyOf(views.get(realObject.getDefaultDisplay().getDisplayId()));
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public Display getDefaultDisplay() {
    if (getApiLevel() > JELLY_BEAN) {
      return reflector(ReflectorWindowManagerImpl.class, realObject).getDefaultDisplay();
    } else {
      return defaultDisplayJB;
    }
  }

  @Implements(className = "android.view.WindowManagerImpl$CompatModeWrapper", maxSdk = JELLY_BEAN)
  public static class ShadowCompatModeWrapper {
    @Implementation(maxSdk = JELLY_BEAN)
    protected Display getDefaultDisplay() {
      return defaultDisplayJB;
    }
  }

  /** Re implement to avoid server call */
  @Implementation(minSdk = R)
  protected WindowInsets getWindowInsetsFromServer(WindowManager.LayoutParams attrs, Rect bounds) {
    Context context = reflector(ReflectorWindowManagerImpl.class, realObject).getContext();
    final Rect systemWindowInsets = new Rect();
    final Rect stableInsets = new Rect();
    final DisplayCutout.ParcelableWrapper displayCutout = new DisplayCutout.ParcelableWrapper();
    final InsetsState insetsState = new InsetsState();
    final boolean alwaysConsumeSystemBars = true;

    final boolean isScreenRound = context.getResources().getConfiguration().isScreenRound();
    if (getApiLevel() <= R
        && reflector(ViewRootImplReflector.class).getNewInsetsMode() == NEW_INSETS_MODE_FULL) {
      return ReflectionHelpers.callInstanceMethod(
          insetsState,
          "calculateInsets",
          ClassParameter.from(Rect.class, bounds),
          null,
          ClassParameter.from(Boolean.TYPE, isScreenRound),
          ClassParameter.from(Boolean.TYPE, alwaysConsumeSystemBars),
          ClassParameter.from(DisplayCutout.ParcelableWrapper.class, displayCutout.get()),
          ClassParameter.from(int.class, SOFT_INPUT_ADJUST_NOTHING),
          ClassParameter.from(int.class, SYSTEM_UI_FLAG_VISIBLE),
          null);
    } else {
      return new WindowInsets.Builder()
          .setAlwaysConsumeSystemBars(alwaysConsumeSystemBars)
          .setRound(isScreenRound)
          .setSystemWindowInsets(Insets.of(systemWindowInsets))
          .setStableInsets(Insets.of(stableInsets))
          .setDisplayCutout(displayCutout.get())
          .build();
    }
  }

  @ForType(WindowManagerImpl.class)
  interface ReflectorWindowManagerImpl {

    @Direct
    void addView(View view, ViewGroup.LayoutParams layoutParams);

    @Direct
    void removeView(View view);

    @Direct
    void removeViewImmediate(View view);

    @Direct
    Display getDefaultDisplay();

    @Accessor("mContext")
    Context getContext();
  }

  @Resetter
  public static void reset() {
    defaultDisplayJB = null;
    views.clear();
    if (getApiLevel() <= VERSION_CODES.JELLY_BEAN) {
      ReflectionHelpers.setStaticField(
          WindowManagerImpl.class,
          "sWindowManager",
          ReflectionHelpers.newInstance(WindowManagerImpl.class));
      HashMap windowManagers =
          ReflectionHelpers.getStaticField(WindowManagerImpl.class, "sCompatWindowManagers");
      windowManagers.clear();
    }
  }
}
