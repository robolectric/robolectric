package org.robolectric.shadows;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;

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

  @Implementation
  public void setSelection(int position) {
    directlyOn(realAbsSpinner, AbsSpinner.class, "setSelection", int.class).invoke(position);
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
