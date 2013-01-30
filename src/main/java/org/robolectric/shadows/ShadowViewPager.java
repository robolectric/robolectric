package org.robolectric.shadows;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(ViewPager.class)
public class ShadowViewPager extends ShadowViewGroup {
    @RealObject
    private ViewPager realViewPager;

    private PagerAdapter adapter;
    private int currentItem;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    @Implementation
    public void setAdapter(PagerAdapter adapter) {
        this.adapter = adapter;
    }

    @Implementation
    public PagerAdapter getAdapter() {
        return adapter;
    }

    @Implementation
    public int getCurrentItem() {
        return currentItem;
    }

    @Implementation
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        onPageChangeListener = listener;
    }

    @Implementation
    public void setCurrentItem(int position) {
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(position);
        }
        currentItem = position;
    }
    
    @Implementation
    public void setCurrentItem(int position, boolean smoothScroll){
    	setCurrentItem( position );
    }
}