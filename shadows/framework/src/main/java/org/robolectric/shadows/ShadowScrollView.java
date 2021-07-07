package org.robolectric.shadows;

import android.widget.ScrollView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ScrollView.class)
public class ShadowScrollView extends ShadowViewGroup {
  @RealObject protected ScrollView realScrollView;

  @Implementation
  protected void smoothScrollTo(int x, int y) {
    scrollTo(x, y);
  }

  @Implementation
  protected void smoothScrollBy(int x, int y) {
    scrollBy(x, y);
  }

  @Implementation
  protected void scrollTo(int x, int y) {
    if (realScrollView.getChildCount() > 0) {
      super.scrollTo(x, y);
    }
  }
}
