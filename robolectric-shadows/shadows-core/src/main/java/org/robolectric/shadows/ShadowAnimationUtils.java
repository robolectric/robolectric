package org.robolectric.shadows;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AnimationUtils.class)
public class ShadowAnimationUtils {

  @Implementation
  public static Interpolator loadInterpolator(Context context, int id) {
    return new LinearInterpolator();
  }

  @Implementation
  public static LayoutAnimationController loadLayoutAnimation(Context context, int id) {
    Animation anim = new TranslateAnimation(0, 0, 30, 0);
    LayoutAnimationController layoutAnim = new LayoutAnimationController(anim);
    Shadows.shadowOf(layoutAnim).setLoadedFromResourceId(id);
    return layoutAnim;
  }
}
