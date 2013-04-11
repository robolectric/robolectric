package org.robolectric.shadows;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = AbsSpinner.class, inheritImplementationMethods = true)
public class ShadowAbsSpinner extends ShadowAdapterView {

    private boolean animatedTransition;

    @Implementation
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override @Implementation
    public SpinnerAdapter getAdapter() {
        return (SpinnerAdapter) super.getAdapter();
    }

    @Implementation
    public void setSelection(int position, boolean animate) {
        super.setSelection(position);
        animatedTransition = animate;
    }

    // Non-implementation helper method
    public boolean isAnimatedTransition() {
        return animatedTransition;
    }
}
