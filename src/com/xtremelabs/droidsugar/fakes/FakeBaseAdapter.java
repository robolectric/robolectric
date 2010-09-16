package com.xtremelabs.droidsugar.fakes;

import android.database.DataSetObserver;
import android.widget.BaseAdapter;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BaseAdapter.class)
public class FakeBaseAdapter {
    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();

    public boolean areAllItemsEnabled() {
        return true;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        dataSetObservers.add(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        dataSetObservers.remove(observer);
    }

    public void notifyDataSetChanged() {
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onChanged();
        }
    }

    public void notifyDataSetInvalidated() {
        for (DataSetObserver dataSetObserver : dataSetObservers) {
            dataSetObserver.onInvalidated();
        }
    }

}
