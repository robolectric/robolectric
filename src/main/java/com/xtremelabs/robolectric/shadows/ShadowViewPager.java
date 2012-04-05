package com.xtremelabs.robolectric.shadows;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ViewPager.class)
public class ShadowViewPager extends ShadowViewGroup {
    @RealObject
    private ViewPager realViewPager;

    private PagerAdapter adapter;

    @Implementation
    public void setAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
        adapter.startUpdate(realViewPager);
        int N = adapter.getCount();
        Object item = adapter.instantiateItem(realViewPager, 0);
        adapter.setPrimaryItem(realViewPager, 0, item);
        adapter.finishUpdate(realViewPager);
    }

    @Implementation
    public PagerAdapter getAdapter() {
        return adapter;
    }
}