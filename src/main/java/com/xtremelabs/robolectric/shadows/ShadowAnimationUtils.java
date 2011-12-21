// Copyright 2011 Square, Inc.
package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * @author Eric Denman (edenman@squareup.com)
 */
@Implements(AnimationUtils.class)
public class ShadowAnimationUtils {
    @Implementation
    public static Animation loadAnimation(Context context, int id) {
        return new Animation() {
        };
    }
}
