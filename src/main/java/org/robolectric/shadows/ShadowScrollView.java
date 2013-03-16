package org.robolectric.shadows;

import android.widget.ScrollView;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(value = ScrollView.class, inheritImplementationMethods = true)
public class ShadowScrollView extends ShadowFrameLayout {
    @Implementation
    public void smoothScrollTo(int x, int y) {
        scrollTo(x, y);
    }
}
