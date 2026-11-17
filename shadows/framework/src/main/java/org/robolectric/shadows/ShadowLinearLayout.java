package org.robolectric.shadows;

import android.widget.LinearLayout;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
  @RealObject LinearLayout realObject;

  @Implementation
  public int getGravity() {
    return ReflectionHelpers.getField(realObject, "mGravity");
  }
}
