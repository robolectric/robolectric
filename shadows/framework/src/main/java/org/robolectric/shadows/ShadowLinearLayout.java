package org.robolectric.shadows;

import android.widget.LinearLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
  @RealObject LinearLayout realObject;

  public int getGravity() {
    return ReflectionHelpers.getField(realObject, "mGravity");
  }
}
