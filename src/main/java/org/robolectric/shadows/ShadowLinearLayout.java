package org.robolectric.shadows;

import android.widget.LinearLayout;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.fest.reflect.core.Reflection.field;

@Implements(LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
  @RealObject LinearLayout realObject;

  public int getGravity() {
    return field("mGravity").ofType(int.class).in(realObject).get();
  }
}
