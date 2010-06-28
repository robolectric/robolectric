package com.xtremelabs.droidsugar.view;

import android.database.DataSetObserver;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ALL"})
public class FakeBaseAdapter {
    private final List<DataSetObserver> dataSetObservers = new ArrayList<DataSetObserver>();

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
