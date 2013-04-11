package org.robolectric.shadows;

import android.view.View;
import android.widget.ViewAnimator;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

/**
 * Shadow of {@link android.widget.ViewAnimator}
 */
@Implements(value = ViewAnimator.class, inheritImplementationMethods = true)
public class ShadowViewAnimator extends ShadowFrameLayout {

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
