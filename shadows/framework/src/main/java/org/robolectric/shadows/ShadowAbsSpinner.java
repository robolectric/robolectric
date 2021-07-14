package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsSpinner.class)
public class ShadowAbsSpinner extends ShadowAdapterView {
  @RealObject AbsSpinner realAbsSpinner;
  private boolean animatedTransition;

  @Implementation
  protected void setSelection(int position, boolean animate) {
    reflector(AbsSpinnerReflector.class, realAbsSpinner).setSelection(position, animate);
    animatedTransition = animate;
  }

  @Implementation
  protected void setSelection(int position) {
    reflector(AbsSpinnerReflector.class, realAbsSpinner).setSelection(position);
    SpinnerAdapter adapter = realAbsSpinner.getAdapter();
    if (getItemSelectedListener() != null && adapter != null) {
      getItemSelectedListener().onItemSelected(realAbsSpinner, null, position, adapter.getItemId(position));
    }
  }

  // Non-implementation helper method
  public boolean isAnimatedTransition() {
    return animatedTransition;
  }

  @ForType(AbsSpinner.class)
  interface AbsSpinnerReflector {

    @Direct
    void setSelection(int position, boolean animate);

    @Direct
    void setSelection(int position);
  }
}
