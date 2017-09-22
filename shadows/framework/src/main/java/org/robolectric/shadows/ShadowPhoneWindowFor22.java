package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.graphics.drawable.Drawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(className = "com.android.internal.policy.impl.PhoneWindow", maxSdk = LOLLIPOP_MR1)
public class ShadowPhoneWindowFor22 extends ShadowPhoneWindow {

  @Override @Implementation(maxSdk = LOLLIPOP_MR1)
  public void setTitle(CharSequence title) {
    this.title = title;
    directlyOn(realWindow, realWindow.getClass().getName(), "setTitle", ReflectionHelpers.ClassParameter.from(CharSequence.class, title));
  }

  @Override @Implementation(maxSdk = LOLLIPOP_MR1)
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    directlyOn(realWindow, realWindow.getClass().getName(), "setBackgroundDrawable", ReflectionHelpers.ClassParameter.from(Drawable.class, drawable));
  }
}
