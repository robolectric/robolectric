package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Window;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.ForType;

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
    reflector(DirectPhoneWindowReflector.class, realWindow).setTitle(title);
  }

  @Implementation(minSdk = M)
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    reflector(DirectPhoneWindowReflector.class, realWindow).setBackgroundDrawable(drawable);
  }

  @Implementation
  protected int getOptionsPanelGravity() {
    return Gravity.CENTER | Gravity.BOTTOM;
  }

  @ForType(className = "com.android.internal.policy.PhoneWindow", direct = true)
  interface DirectPhoneWindowReflector {

    void setTitle(CharSequence title);

    void setBackgroundDrawable(Drawable drawable);
  }
}
