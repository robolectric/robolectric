package com.xtremelabs.droidsugar.fakes;

import android.database.AbstractCursor;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbstractCursor.class)
public class FakeAbstractCursor {
    private AbstractCursor real;

    public FakeAbstractCursor(AbstractCursor real) {
        this.real = real;
    }

    @Implementation
    public final boolean moveToFirst() {
        return real.getCount() > 0;
    }
}
