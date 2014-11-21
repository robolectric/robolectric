package org.robolectric.shadows;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import static org.robolectric.util.ShadowThingy.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsSpinner.class)
public class ShadowAbsSpinner extends ShadowAdapterView {
  @RealObject AbsSpinner realAbsSpinner;
  private boolean animatedTransition;

  @Implementation
  public void setSelection(int position, boolean animate) {
    directlyOn(realAbsSpinner, AbsSpinner.class, "setSelection", new ReflectionHelpers.ClassParameter(int.class, position), new ReflectionHelpers.ClassParameter(boolean.class, animate));
    animatedTransition = animate;
  }

  @Implementation
  public void setSelection(int position) {
    directlyOn(realAbsSpinner, AbsSpinner.class, "setSelection", new ReflectionHelpers.ClassParameter(int.class, position));
    SpinnerAdapter adapter = realAbsSpinner.getAdapter();
    if (getItemSelectedListener() != null && adapter != null) {
      getItemSelectedListener().onItemSelected(realAbsSpinner, null, position, adapter.getItemId(position));
    }
  }

  // Non-implementation helper method
  public boolean isAnimatedTransition() {
    return animatedTransition;
  }
}
