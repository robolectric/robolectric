package com.xtremelabs.droidsugar.fakes;

import android.widget.AbsSpinner;
import android.widget.SpinnerAdapter;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbsSpinner.class)
public class FakeAbsSpinner extends FakeAdapterView {
    private SpinnerAdapter adapter;

    public FakeAbsSpinner(AbsSpinner absSpinner) {
        super(absSpinner);
    }

    public void setAdapter(SpinnerAdapter adapter) {
        this.adapter = adapter;
    }

    public SpinnerAdapter getAdapter() {
        return adapter;
    }
}
