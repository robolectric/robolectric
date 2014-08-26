package org.robolectric.shadows;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link android.widget.ViewAnimator}
 */
@Implements(ViewAnimator.class)
public class ShadowViewAnimator extends ShadowFrameLayout {

  private int currentChild = 0;

  @Implementation
  public int getDisplayedChild() {
    return currentChild;
  }

  @Implementation
  public void setDisplayedChild(int whichChild) {
    currentChild = whichChild;
    for (int i = ((ViewGroup) realView).getChildCount() - 1; i >= 0; i--) {
      View child = ((ViewGroup) realView).getChildAt(i);
      child.setVisibility(i == whichChild ? View.VISIBLE : View.GONE);
    }
  }

  @Implementation
  public View getCurrentView() {
    return ((ViewGroup) realView).getChildAt(getDisplayedChild());
  }

  @Implementation
  public void showNext() {
    setDisplayedChild((getDisplayedChild() + 1) % ((ViewGroup) realView).getChildCount());
  }

  @Implementation
  public void showPrevious() {
    setDisplayedChild(getDisplayedChild() == 0 ? ((ViewGroup) realView).getChildCount() - 1 : getDisplayedChild() - 1);
  }
}
