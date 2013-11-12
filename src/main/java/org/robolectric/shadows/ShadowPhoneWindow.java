package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.view.Window;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static org.robolectric.Robolectric.directlyOn;

@Implements(value = Robolectric.Anything.class, className = ShadowPhoneWindow.PHONE_WINDOW_CLASS_NAME)
public class ShadowPhoneWindow extends ShadowWindow {
  public static final String PHONE_WINDOW_CLASS_NAME = "com.android.internal.policy.impl.PhoneWindow";

  private CharSequence title;
  private Drawable backgroundDrawable;

  @Implementation
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "setTitle", CharSequence.class).invoke(title);
  }

  public CharSequence getTitle() {
    return title;
  }

  @Implementation
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, PHONE_WINDOW_CLASS_NAME, "setBackgroundDrawable", Drawable.class).invoke(drawable);
  }

  public Drawable getBackgroundDrawable() {
    return backgroundDrawable;
  }
}
