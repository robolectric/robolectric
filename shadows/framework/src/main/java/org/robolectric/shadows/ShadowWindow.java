package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Window;
import android.widget.ProgressBar;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Window.class)
public class ShadowWindow {
  private @RealObject Window realWindow;

  protected CharSequence title = "";
  protected Drawable backgroundDrawable;
  private int flags;
  private int softInputMode;

  public static Window create(Context context) throws Exception {
    String className = getApiLevel() >= M
        ? "com.android.internal.policy.PhoneWindow"
        : "com.android.internal.policy.impl.PhoneWindow";
    Class<? extends Window> phoneWindowClass =
        (Class<? extends Window>) Window.class.getClassLoader().loadClass(className);
    return ReflectionHelpers.callConstructor(phoneWindowClass, ClassParameter.from(Context.class, context));
  }

  @Implementation
  protected void setFlags(int flags, int mask) {
    this.flags = (this.flags & ~mask) | (flags & mask);
    directlyOn(realWindow, Window.class, "setFlags", ClassParameter.from(int.class, flags), ClassParameter.from(int.class, mask));
  }

  @Implementation
  protected void setSoftInputMode(int softInputMode) {
    this.softInputMode = softInputMode;
    directlyOn(realWindow, Window.class, "setSoftInputMode", ClassParameter.from(int.class, softInputMode));
  }

  public boolean getFlag(int flag) {
    return (flags & flag) == flag;
  }

  public CharSequence getTitle() {
    return title;
  }

  public int getSoftInputMode() {
    return softInputMode;
  }

  public Drawable getBackgroundDrawable() {
    return backgroundDrawable;
  }

  public ProgressBar getProgressBar() {
    return (ProgressBar) directlyOn(realWindow, realWindow.getClass().getName(), "getHorizontalProgressBar", ClassParameter.from(boolean.class, false));
  }

  public ProgressBar getIndeterminateProgressBar() {
    return (ProgressBar) directlyOn(realWindow, realWindow.getClass().getName(), "getCircularProgressBar", ClassParameter.from(boolean.class, false));
  }
}
