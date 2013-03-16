package org.robolectric.shadows;

import android.widget.CheckedTextView;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = CheckedTextView.class, inheritImplementationMethods = true)
public class ShadowCheckedTextView extends ShadowTextView {
    @RealObject CheckedTextView realCheckedTextView;
    private boolean checked;

    @Implementation
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Implementation
    public boolean isChecked() {
        return checked;
    }

    @Implementation @Override
    public boolean performClick() {
        realCheckedTextView.toggle();
        return super.performClick();
    }

    @Implementation
    public void toggle() {
        realCheckedTextView.setChecked(!realCheckedTextView.isChecked());
    }
}
