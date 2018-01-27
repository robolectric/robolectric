package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.graphics.drawable.Drawable;
import android.view.Window;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(className = "com.android.internal.policy.PhoneWindow", isInAndroidSdk = false, minSdk = M)
public class ShadowPhoneWindow extends ShadowWindow {
  @SuppressWarnings("UnusedDeclaration")
  protected @RealObject Window realWindow;

  @Implementation(minSdk = M)
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, realWindow.getClass().getName(), "setTitle", ClassParameter.from(CharSequence.class, title));
  }

  @Implementation(minSdk = M)
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, realWindow.getClass().getName(), "setBackgroundDrawable", ClassParameter.from(Drawable.class, drawable));
  }
}
