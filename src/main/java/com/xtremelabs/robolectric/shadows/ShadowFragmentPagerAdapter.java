package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(FragmentPagerAdapter.class)
public class ShadowFragmentPagerAdapter extends ShadowPagerAdapter{
    @RealObject
    private FragmentPagerAdapter realAdapter;

    private FragmentManager fragmentManager;

    @Implementation
    public void __constructor__(FragmentManager fm) {
        this.fragmentManager = fm;
    }

    @Implementation
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        fragmentManager.beginTransaction().add(container.getId(), (Fragment) object).commit();
    }

    @Implementation
    public Object instantiateItem(ViewGroup container, int position) {
        return realAdapter.getItem(position);
    }
}