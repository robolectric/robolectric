package com.xtremelabs.robolectric.shadows;

import android.widget.TabHost;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.lang.reflect.Constructor;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(TabHost.class)
public class ShadowTabHost extends ShadowFrameLayout {

    @Implementation
    public android.widget.TabHost.TabSpec newTabSpec(java.lang.String tag) {
        TabHost.TabSpec realTabSpec = null;
        try {
            Constructor<TabHost.TabSpec> c = TabHost.TabSpec.class.getDeclaredConstructor();
            c.setAccessible(true);
            realTabSpec = c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        shadowOf(realTabSpec).setTag(tag);
        return realTabSpec;
    }
}
