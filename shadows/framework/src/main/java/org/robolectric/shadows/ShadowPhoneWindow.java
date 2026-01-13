package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Window;
import com.android.internal.policy.PhoneWindow;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.ForType;

/** Shadow for PhoneWindow */
@Implements(value = PhoneWindow.class, isInAndroidSdk = false)
public class ShadowPhoneWindow extends ShadowWindow {
  protected @RealObject Window realWindow;
  protected boolean decorFitsSystemWindows = true;

  @Implementation
  public void setTitle(CharSequence title) {
    this.title = title;
    reflector(DirectPhoneWindowReflector.class, realWindow).setTitle(title);
  }

  @Implementation
  public void setBackgroundDrawable(Drawable drawable) {
    this.backgroundDrawable = drawable;
    reflector(DirectPhoneWindowReflector.class, realWindow).setBackgroundDrawable(drawable);
  }

  @Implementation
  protected int getOptionsPanelGravity() {
    return Gravity.CENTER | Gravity.BOTTOM;
  }

  @Implementation(minSdk = R)
  protected void setDecorFitsSystemWindows(boolean decorFitsSystemWindows) {
    this.decorFitsSystemWindows = decorFitsSystemWindows;
    reflector(DirectPhoneWindowReflector.class, realWindow)
        .setDecorFitsSystemWindows(decorFitsSystemWindows);
  }

  /**
   * Returns true with the last value passed to {@link #setDecorFitsSystemWindows(boolean)}, or the
   * default value (true).
   */
  @RequiresApi(R)
  public boolean getDecorFitsSystemWindows() {
    return decorFitsSystemWindows;
  }

  @ForType(value = PhoneWindow.class, direct = true)
  interface DirectPhoneWindowReflector {

    void setTitle(CharSequence title);

    void setBackgroundDrawable(Drawable drawable);

    void setDecorFitsSystemWindows(boolean decorFitsSystemWindows);
  }
}
