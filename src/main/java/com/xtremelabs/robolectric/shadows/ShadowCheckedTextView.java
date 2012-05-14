package com.xtremelabs.robolectric.shadows;

import android.widget.CheckedTextView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(CheckedTextView.class)
public class ShadowCheckedTextView extends ShadowTextView {

    private boolean checked;

    @Implementation
    public void toggle() {
        checked = !checked;
    }

    @Implementation
    public boolean isChecked() {
        return checked;
    }

    @Implementation
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
