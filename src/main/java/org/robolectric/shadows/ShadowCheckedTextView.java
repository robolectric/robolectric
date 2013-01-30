package com.xtremelabs.robolectric.shadows;

import android.widget.CheckedTextView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CheckedTextView.class)
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
