package org.robolectric.shadows;

import android.widget.AbsSpinner;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsSpinner.class)
public class ShadowAbsSpinner extends ShadowAdapterView {
  @RealObject AbsSpinner realAbsSpinner;
  private boolean animatedTransition;

  @Implementation
  public void setSelection(int position, boolean animate) {
    directlyOn(realAbsSpinner, AbsSpinner.class, "setSelection", int.class, boolean.class)
        .invoke(position, animate);
    animatedTransition = animate;
  }

  // Non-implementation helper method
  public boolean isAnimatedTransition() {
    return animatedTransition;
  }
}
