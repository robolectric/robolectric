package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ViewAnimator;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ViewAnimator.class)
public class ShadowViewAnimator extends ShadowFrameLayout {
    private int mWhichChild = 0;

    @Implementation
    public void showNext() {
        setDisplayedChild(mWhichChild + 1);
    }

    @Implementation
    public void showPrevious() {
        setDisplayedChild(mWhichChild - 1);
    }

    @Implementation
    public void setDisplayedChild(int whichChild) {
        mWhichChild = whichChild;
        if (whichChild >= getChildCount()) {
            mWhichChild = 0;
        } else if (whichChild < 0) {
            mWhichChild = getChildCount() - 1;
        }
    }

    @Implementation
    public int getDisplayedChild() {
        return mWhichChild;
    }

    @Implementation
    public View getCurrentView() {
        return getChildAt(mWhichChild);
    }
}
