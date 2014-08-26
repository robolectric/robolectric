package org.robolectric.shadows;

import android.widget.AbsoluteLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsoluteLayout.class)
public class ShadowAbsoluteLayout extends ShadowViewGroup {
  @Implements(AbsoluteLayout.LayoutParams.class)
  public static class ShadowLayoutParams extends ShadowViewGroup.ShadowLayoutParams {
    @RealObject
    AbsoluteLayout.LayoutParams realLayoutParams;

    public void __constructor__(int width, int height, int x, int y) {
      realLayoutParams.width = width;
      realLayoutParams.height = height;
      realLayoutParams.x = x;
      realLayoutParams.y = y;
    }
  }
}
