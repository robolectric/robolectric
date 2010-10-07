package com.xtremelabs.droidsugar.fakes;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class FakeBaseAdapter {
    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();

    @Implementation
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Implementation
    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservers.add(observer);
    }

    @Implementation
    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservers.remove(observer);
    }

    @Implementation
    public void notifyDataSetChanged() {
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onChanged();
        }
    }

    @Implementation
    public void notifyDataSetInvalidated() {
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onInvalidated();
        }
    }

}
