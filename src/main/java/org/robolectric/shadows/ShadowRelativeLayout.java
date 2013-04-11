package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.res.Attribute;

import java.util.ArrayList;

import static org.robolectric.Robolectric.shadowOf;

@Implements(value = RelativeLayout.class, inheritImplementationMethods = true)
public class ShadowRelativeLayout extends ShadowViewGroup {

    public void __constructor__(Context context) {
        __constructor__(context, shadowOf(context).createAttributeSet(new ArrayList<Attribute>(), null), 0);
    }

    public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
        setLayoutParams(new ViewGroup.MarginLayoutParams(0, 0));
        super.__constructor__(context, attributeSet, defStyle);
    }

    /**
     * Shadow for {@link android.view.ViewGroup.MarginLayoutParams} that simulates its implementation.
     */
    @SuppressWarnings("UnusedDeclaration")
    @Implements(RelativeLayout.LayoutParams.class)
    public static class ShadowLayoutParams extends ShadowMarginLayoutParams {
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
}
