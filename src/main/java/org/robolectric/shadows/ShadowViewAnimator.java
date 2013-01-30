package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ViewAnimator;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@link android.widget.ViewAnimator}
 */
@Implements(ViewAnimator.class)
public class ShadowViewAnimator extends ShadowViewGroup {

    private int currentChild = 0;

    @Implementation
    public int getDisplayedChild() {
        return currentChild;
    }

    @Implementation
    public void setDisplayedChild(int whichChild) {
        currentChild = whichChild;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            child.setVisibility(i == whichChild ? View.VISIBLE : View.GONE);
        }
    }

    @Implementation
    public View getCurrentView() {
        return getChildAt(getDisplayedChild());
    }

    @Implementation
    public void showNext() {
        setDisplayedChild((getDisplayedChild() + 1) % getChildCount());
    }

    @Implementation
    public void showPrevious() {
        setDisplayedChild(getDisplayedChild() == 0 ? getChildCount() - 1 : getDisplayedChild() - 1);
    }
}
