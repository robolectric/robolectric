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
    if (!useRealSpinnerSelection()) {
      // Use the broken legacy spinner selection callback invocation logic.
      SpinnerAdapter adapter = realAbsSpinner.getAdapter();
      if (getItemSelectedListener() != null && adapter != null) {
        getItemSelectedListener()
            .onItemSelected(realAbsSpinner, null, position, adapter.getItemId(position));
      }
    }
  }

  /**
   * Currently the shadow method {@link #setSelection(int)} broken and low-fidelity. In real
   * Android, the item selected callback is invoked during layout passes. However, in Robolectric,
   * the callback is always invoked explicitly, which leads to many issues:
   *
   * <ol>
   *   <li>Item selection callbacks are invoked even if the selection hasn't changed, which can lead
   *       to infinite loops.
   *   <li>Item selection callbacks are still invoked if spinners are not attached to an Activity.
   *   <li>Item selection callbacks are invoked even if spinners are non-visible.
   *   <li>Item selection callbacks are invoked twice if spinners are attached to an Activity and
   *       visible.
   * </ol>
   *
   * <p>Long-term we want to eliminate this broken shadow method, but in the mean time the real
   * scrolling behavior is only enabled if a system property is set, due to tests depending on the
   * broken behavior.
   */
  private static boolean useRealSpinnerSelection() {
    return Boolean.getBoolean("robolectric.useRealSpinnerSelection");
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
