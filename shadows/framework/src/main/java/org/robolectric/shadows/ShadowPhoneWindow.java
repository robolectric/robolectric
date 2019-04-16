package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadows.ShadowBaseLooper.shadowMainLooper;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewRootImpl;
import android.view.Window;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow for PhoneWindow for APIs 23+
 */
@Implements(className = "com.android.internal.policy.PhoneWindow", isInAndroidSdk = false,
    minSdk = M, looseSignatures = true)
public class ShadowPhoneWindow extends ShadowWindow {
  @SuppressWarnings("UnusedDeclaration")
  protected @RealObject Window realWindow;

  @Implementation(minSdk = M)
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, realWindow.getClass().getName(), "setTitle",
        ClassParameter.from(CharSequence.class, title));
  }

  @Implementation(minSdk = M)
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, realWindow.getClass().getName(), "setBackgroundDrawable",
        ClassParameter.from(Drawable.class, drawable));
  }

  @Implementation
  protected int getOptionsPanelGravity() {
    return Gravity.CENTER | Gravity.BOTTOM;
  }

  protected String getPanelFeatureClassName() {
    return "com.android.internal.policy.PhoneWindow$PanelFeatureState";
  }

  @Implementation(minSdk = M)
  protected void openPanel(final /* int OR PanelFeatureState */ Object panelFeatureStateOrFeatureId,
      /* KeyEvent */ Object event) {
    // unfortunately, looseSignatures need to be used here since PanelFeatureState was moved in
    // API 23. And there are two variants of openPanel methods.
    // Do a runtime type check to see what type first param is, to get the appropriate
    // ClassParameter
    Class<?> firstParamClass = getOpenPanelFirstType(panelFeatureStateOrFeatureId);
    directlyOn(realWindow, realWindow.getClass().getName(), "openPanel",
        ClassParameter
            .from(firstParamClass, panelFeatureStateOrFeatureId),
        ClassParameter.from(KeyEvent.class, event));

    if (firstParamClass.getName().contains("PanelFeatureState")) {
      shadowMainLooper().idleIfPaused();
      Object decorViewObj = ReflectionHelpers.getField(panelFeatureStateOrFeatureId, "decorView");
      ViewRootImpl viewRoot = ReflectionHelpers.callInstanceMethod(decorViewObj,
          "getViewRootImpl");
      if (viewRoot != null) {
        ReflectionHelpers.callInstanceMethod(viewRoot, "windowFocusChanged",
            from(boolean.class, true), /* hasFocus */
            from(boolean.class, false) /* inTouchMode */);
        shadowMainLooper().idleIfPaused();
      }

    }
  }

  private Class<?> getOpenPanelFirstType(Object  panelFeatureStateOrFeatureId) {
    // there are two variants of openPanel methods. Do a runtime type check to see what type
    // first param is, to return the appropriate ClassParameter
    if (panelFeatureStateOrFeatureId.getClass().isAssignableFrom(Integer.class)) {
      return int.class;
    }
    try {
      return Class.forName(getPanelFeatureClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("could not load PhoneWindow$PanelFeatureState class", e);
    }
  }
}
