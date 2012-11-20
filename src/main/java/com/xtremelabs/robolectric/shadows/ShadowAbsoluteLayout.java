package com.xtremelabs.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsoluteLayout.class)
public class ShadowAbsoluteLayout extends ShadowViewGroup {
    private AbsoluteLayout.LayoutParams layoutParams = new AbsoluteLayout.LayoutParams(0, 0, 0, 0);

    @Implementation
    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return layoutParams;
    }

    @Implements(AbsoluteLayout.LayoutParams.class)
    public static class ShadowLayoutParams {
        @RealObject
        AbsoluteLayout.LayoutParams realLayoutParams;

        public void __constructor__(int width, int height, int x, int y) {
            realLayoutParams.width = width;
            realLayoutParams.height = height;
            realLayoutParams.x = x;
            realLayoutParams.y = y;
        }
    }

    @Override
    @Implementation
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new AbsoluteLayout.LayoutParams(0, 0, 0, 0);
    }
}
