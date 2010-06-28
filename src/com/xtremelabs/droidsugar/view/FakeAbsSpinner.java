package com.xtremelabs.droidsugar.view;

import android.widget.*;

@SuppressWarnings({"UnusedDeclaration"})
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
