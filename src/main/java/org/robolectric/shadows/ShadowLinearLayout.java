package org.robolectric.shadows;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
    private int orientation;
    private int gravity = Gravity.TOP | Gravity.START;

    public ShadowLinearLayout() {
        setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    @Override
    @Implementation
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LinearLayout.LayoutParams(0, 0);
    }

    @Implementation
    public int getOrientation() {
        return orientation;
    }

    @Implementation
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getGravity() {
        return gravity;
    }

    @Implementation
    public void setGravity(int gravity) {
        this.gravity = gravity;
    }
}
