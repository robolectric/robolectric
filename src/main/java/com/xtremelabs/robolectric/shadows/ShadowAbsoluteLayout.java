package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.AbsoluteLayout;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsoluteLayout.class)
public class ShadowAbsoluteLayout extends ShadowViewGroup {
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
    protected void setChildLayoutParams(View child) {
        child.setLayoutParams(new AbsoluteLayout.LayoutParams(0, 0, 0, 0));
    }
}
