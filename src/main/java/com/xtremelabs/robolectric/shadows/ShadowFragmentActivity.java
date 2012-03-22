package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.tester.android.util.TestFragmentManager;

@Implements(FragmentActivity.class)
public class ShadowFragmentActivity extends ShadowActivity {
    @RealObject
    FragmentActivity realObject;

    private FragmentManager fragmentManager;

    public void __constructor__() {
        fragmentManager = new TestFragmentManager(realObject);
    }

    @Implementation
    public FragmentManager getSupportFragmentManager() {
        return fragmentManager;
    }

}
