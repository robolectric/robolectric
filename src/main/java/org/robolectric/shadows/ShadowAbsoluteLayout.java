package org.robolectric.shadows;

import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsoluteLayout.class)
public class ShadowAbsoluteLayout extends ShadowViewGroup {
    @Implements(AbsoluteLayout.LayoutParams.class)
    public static class ShadowLayoutParams extends org.robolectric.shadows.ShadowLayoutParams {
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
