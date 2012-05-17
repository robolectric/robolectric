package com.xtremelabs.robolectric.shadows;

import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(ViewPager.class)
public class ShadowViewPager extends ShadowViewGroup {
    @RealObject
    private ViewPager realViewPager;

    private PagerAdapter adapter;
    private int currentItem;

    @Implementation
    public void setAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
        shadowOf(adapter).registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                dataSetChanged();
            }
        });
        dataSetChanged();
    }

    @Implementation
    public PagerAdapter getAdapter() {
        return adapter;
    }

    @Implementation
    public void dataSetChanged() {
        while (getChildCount() < adapter.getCount()) {
            adapter.startUpdate(realViewPager);
            Object item = adapter.instantiateItem(realViewPager, getChildCount());
            adapter.setPrimaryItem(realViewPager, 0, item);
            adapter.finishUpdate(realViewPager);
        }
    }

    @Implementation
    public int getCurrentItem() {
        return currentItem;
    }

    @Implementation
    public void setCurrentItem(int item) {
        currentItem = item;
    }

    public View getCurrentView() {
        return getChildAt(getCurrentItem());
    }
}