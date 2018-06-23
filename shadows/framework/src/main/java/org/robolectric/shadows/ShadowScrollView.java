package org.robolectric.shadows;

import android.widget.ScrollView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(ScrollView.class)
public class ShadowScrollView extends ShadowViewGroup {

  @Implementation
  public void smoothScrollTo(int x, int y) {
    scrollTo(x, y);
  }
}
