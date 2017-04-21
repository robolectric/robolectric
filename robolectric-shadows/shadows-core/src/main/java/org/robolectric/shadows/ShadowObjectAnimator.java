package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import org.robolectric.annotation.Implements;

@Implements(ObjectAnimator.class)
public class ShadowObjectAnimator extends ShadowValueAnimator {
}
