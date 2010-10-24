package com.xtremelabs.robolectric.shadows;

import android.database.AbstractCursor;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    private AbstractCursor real;

    public ShadowAbstractCursor(AbstractCursor real) {
        this.real = real;
    }

    @Implementation
    public final boolean moveToFirst() {
        return real.getCount() > 0;
    }
}
