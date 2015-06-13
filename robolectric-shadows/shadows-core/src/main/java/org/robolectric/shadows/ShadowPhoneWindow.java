package org.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.widget.ProgressBar;
import com.android.internal.policy.impl.PhoneWindow;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link com.android.internal.policy.impl.PhoneWindow}.
 */
@Implements(value = PhoneWindow.class, isInAndroidSdk = false)
public class ShadowPhoneWindow extends ShadowWindow {
  @SuppressWarnings("UnusedDeclaration")
  private @RealObject PhoneWindow realWindow;

  private CharSequence title;
  private Drawable backgroundDrawable;

  @Implementation
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, PhoneWindow.class, "setTitle", ClassParameter.from(CharSequence.class, title));
  }

  public CharSequence getTitle() {
    return title;
  }

  @Implementation
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, PhoneWindow.class, "setBackgroundDrawable", ClassParameter.from(Drawable.class, drawable));
  }

  public Drawable getBackgroundDrawable() {
    return backgroundDrawable;
  }

  @Override
  public ProgressBar getProgressBar() {
    return (ProgressBar) directlyOn(realWindow, PhoneWindow.class, "getHorizontalProgressBar", ClassParameter.from(boolean.class, false));
  }

  @Override
  public ProgressBar getIndeterminateProgressBar() {
    return (ProgressBar) directlyOn(realWindow, PhoneWindow.class, "getCircularProgressBar", ClassParameter.from(boolean.class, false));
  }
}
