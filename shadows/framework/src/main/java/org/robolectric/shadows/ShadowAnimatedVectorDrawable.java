package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.TargetApi;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;


@Implements(value = AnimatedVectorDrawable.class, minSdk = N)
public class ShadowAnimatedVectorDrawable extends ShadowVectorDrawable {

  @RealObject private AnimatedVectorDrawable realAnimatedVectorDrawable;

  /**
   * Makes sure animations are run with {@link android.animation.AnimatorSet} on the UI thread instead of natively
   */
  @TargetApi(LOLLIPOP)
  @Implementation
  public void start() {
    realAnimatedVectorDrawable.forceAnimationOnUI();
    directlyOn(realAnimatedVectorDrawable, AnimatedVectorDrawable.class).start();
  }
}
