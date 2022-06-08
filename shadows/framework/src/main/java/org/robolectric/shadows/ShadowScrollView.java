package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.ScrollView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ScrollView.class)
public class ShadowScrollView extends ShadowViewGroup {

  @RealObject ScrollView realScrollView;

  @Implementation
  protected void smoothScrollTo(int x, int y) {
    if (useRealGraphics()) {
      reflector(ScrollViewReflector.class, realScrollView).smoothScrollTo(x, y);
    } else {
      scrollTo(x, y);
    }
  }

  @Implementation
  protected void smoothScrollBy(int x, int y) {
    if (useRealGraphics()) {
      reflector(ScrollViewReflector.class, realScrollView).smoothScrollBy(x, y);
    } else {
      scrollBy(x, y);
    }
  }

  @ForType(ScrollView.class)
  interface ScrollViewReflector {
    @Direct
    void smoothScrollBy(int x, int y);

    @Direct
    void smoothScrollTo(int x, int y);
  }
}
