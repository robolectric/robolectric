package com.xtremelabs.robolectric.shadows;

import android.widget.RelativeLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow for {@link android.view.ViewGroup.MarginLayoutParams} that simulates its implementation.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(RelativeLayout.LayoutParams.class)
public class ShadowRelativeLayoutParams extends ShadowMarginLayoutParams {
    int rules[] = new int[16]; // not the right way to do this

    @Implementation
    public void addRule(int verb) {
        addRule(verb, -1);
    }

    @Implementation
    public void addRule(int verb, int anchor) {
        rules[verb] = anchor;
    }

    @Implementation
    public int[] getRules() {
        return rules;
    }
}
