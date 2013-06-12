package org.robolectric.shadows;

import android.view.Gravity;
import android.widget.LinearLayout;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = LinearLayout.class)
public class ShadowLinearLayout extends ShadowViewGroup {
  private int orientation;
  private int gravity = Gravity.TOP | Gravity.START;

  @Implementation
  public int getOrientation() {
    return orientation;
  }

  @Implementation
  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }

  public int getGravity() {
    return gravity;
  }

  @Implementation
  public void setGravity(int gravity) {
    this.gravity = gravity;
  }
}
