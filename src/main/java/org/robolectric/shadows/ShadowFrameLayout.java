package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

/**
 * Shadow for {@link FrameLayout} that simulates its implementation.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(value = FrameLayout.class, inheritImplementationMethods = true)
public class ShadowFrameLayout extends ShadowViewGroup {
    private FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);

    public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
        setLayoutParams(new ViewGroup.MarginLayoutParams(0, 0));
        super.__constructor__(context, attributeSet, defStyle);
    }
    
    @Implementation
    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    @Override
    @Implementation
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new FrameLayout.LayoutParams(0, 0);
    }
}
