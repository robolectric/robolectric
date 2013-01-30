package com.xtremelabs.robolectric.shadows;

import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(PagerAdapter.class)
public class ShadowPagerAdapter {
    private DataSetObserver dataSetObserver;

    @Implementation
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.dataSetObserver = dataSetObserver;
    }

    @Implementation
    public void notifyDataSetChanged() {
        if (dataSetObserver != null) {
            dataSetObserver.onChanged();
        }
    }
}
