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
    // VERB_COUNT is defined in RelativeLayout.java and is private, so we duplicate it here.
    private static final int VERB_COUNT = 16;

    private int rules[] = new int[VERB_COUNT];

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
