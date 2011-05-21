package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.TabHost;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabHost.class)
public class ShadowTabHost extends ShadowViewGroup {

    @Implementation
    public TabHost.TabSpec newTabSpec(String tag) {
        return Robolectric.newInstanceOf(TabHost.TabSpec.class);
    }

}
