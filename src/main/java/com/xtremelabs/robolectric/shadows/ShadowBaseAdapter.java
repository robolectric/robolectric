package com.xtremelabs.robolectric.shadows;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class ShadowBaseAdapter {
	@RealObject private BaseAdapter realBaseAdapter;
    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();
    private boolean wasNotifyDataSetChangedCalled;

    @Implementation
    public boolean isEmpty() {
    	return realBaseAdapter.getCount() == 0;
    }

    /**
     * Just returns true
     *
     * @return true
     */
    @Implementation
    public boolean areAllItemsEnabled() {
        return true;
    }

    /**
     * Registers the observer.
     *
     * @param observer observer
     */
    @Implementation
    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservers.add(observer);
    }

    /**
     * Unregisters the observer if it can be found. Nothing otherwise.
     *
     * @param observer observer
     */
    @Implementation
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservers.remove(observer);
    }

    /**
     * Notifies the registered observers
     */
    @Implementation
    public void notifyDataSetChanged() {
        wasNotifyDataSetChangedCalled = true;
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onChanged();
        }
    }

    /**
     * Notifies the registered observers
     */
    @Implementation
    public void notifyDataSetInvalidated() {
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onInvalidated();
        }
    }

    public void clearWasDataSetChangedCalledFlag() {
        wasNotifyDataSetChangedCalled = false;
    }

    public boolean wasNotifyDataSetChangedCalled() {
        return wasNotifyDataSetChangedCalled;
    }
}
