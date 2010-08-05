package com.xtremelabs.droidsugar.fakes;

import android.database.AbstractCursor;

public class FakeAbstractCursor {
    private AbstractCursor real;

    public FakeAbstractCursor(AbstractCursor real) {
        this.real = real;
    }

    public final boolean moveToFirst() {
        return real.getCount() > 0;
    }
}
