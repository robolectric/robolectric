package com.xtremelabs.robolectric.shadows;

import android.database.AbstractCursor;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

/**
 * Shadows the {@code android.database.AbstractCursor} class.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    @RealObject private AbstractCursor realAbstractCursor;

    @Implementation
    public final boolean moveToFirst() {
        return realAbstractCursor.getCount() > 0;
    }
}
