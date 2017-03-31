package org.robolectric.shadows;

import android.animation.ObjectAnimator;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.animation.ObjectAnimator}.
 */
@Implements(ObjectAnimator.class)
public class ShadowObjectAnimator extends ShadowValueAnimator {
}
