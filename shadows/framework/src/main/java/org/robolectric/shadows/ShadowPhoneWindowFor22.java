package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.Drawable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for the API 16-22 PhoneWindow.li
 */
@Implements(className = "com.android.internal.policy.impl.PhoneWindow", maxSdk = LOLLIPOP_MR1,
    looseSignatures = true, isInAndroidSdk = false)
public class ShadowPhoneWindowFor22 extends ShadowPhoneWindow {

  @Override @Implementation(maxSdk = LOLLIPOP_MR1)
  public void setTitle(CharSequence title) {
    this.title = title;
    reflector(DirectPhoneWindowFor22Reflector.class, realWindow).setTitle(title);
  }

  @Override @Implementation(maxSdk = LOLLIPOP_MR1)
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    reflector(DirectPhoneWindowFor22Reflector.class, realWindow).setBackgroundDrawable(drawable);
  }

  @Override @Implementation(maxSdk = LOLLIPOP_MR1)
  protected int getOptionsPanelGravity() {
    return super.getOptionsPanelGravity();
  }

  @ForType(className = "com.android.internal.policy.impl.PhoneWindow", direct = true)
  interface DirectPhoneWindowFor22Reflector extends DirectPhoneWindowReflector {}
}
