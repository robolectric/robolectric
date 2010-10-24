package com.xtremelabs.robolectric.shadows;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsSpinner.class)
public class ShadowAbsSpinner extends ShadowAdapterView {
    public ShadowAbsSpinner(AbsSpinner absSpinner) {
        super(absSpinner);
    }

    @Implementation
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override @Implementation
    public SpinnerAdapter getAdapter() {
        return (SpinnerAdapter) adapter;
    }
}
