package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

import static org.robolectric.internal.Shadow.directlyOn;

@Implements(className = ShadowPhoneWindow.PHONE_WINDOW_CLASS_NAME)
public class ShadowPhoneWindow extends ShadowWindow {
  public static final String PHONE_WINDOW_CLASS_NAME = "com.android.internal.policy.impl.PhoneWindow";

  private CharSequence title;
  private Drawable backgroundDrawable;

  @Implementation
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "setTitle", new ReflectionHelpers.ClassParameter(CharSequence.class, title));
  }

  public CharSequence getTitle() {
    return title;
  }

  @Implementation
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "setBackgroundDrawable", new ReflectionHelpers.ClassParameter(Drawable.class, drawable));
  }

  public Drawable getBackgroundDrawable() {
    return backgroundDrawable;
  }

  @Override
  public ProgressBar getProgressBar() {
    return (ProgressBar) directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "getHorizontalProgressBar", new ReflectionHelpers.ClassParameter(boolean.class, false));
  }

  @Override
  public ProgressBar getIndeterminateProgressBar() {
    return (ProgressBar) directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "getCircularProgressBar", new ReflectionHelpers.ClassParameter(boolean.class, false));
  }
}
