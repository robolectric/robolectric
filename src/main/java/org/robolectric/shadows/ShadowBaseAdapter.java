package org.robolectric.shadows;

import android.widget.BaseAdapter;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = BaseAdapter.class, callThroughByDefault = true)
public class ShadowBaseAdapter {
    @RealObject private BaseAdapter realBaseAdapter;
    private boolean wasNotifyDataSetChangedCalled;

    @Implementation
    public void notifyDataSetChanged() {
        wasNotifyDataSetChangedCalled = true;
        directlyOn(realBaseAdapter, BaseAdapter.class, "notifyDataSetChanged").invoke();
    }

    public void clearWasDataSetChangedCalledFlag() {
        wasNotifyDataSetChangedCalled = false;
    }

    public boolean wasNotifyDataSetChangedCalled() {
        return wasNotifyDataSetChangedCalled;
    }
}
