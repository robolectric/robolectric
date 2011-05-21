package com.xtremelabs.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabActivity.class)
public class ShadowTabActivity extends ShadowActivity {

    @RealObject TabActivity realTabActivity;

    @Implementation
    public TabHost getTabHost() {
        return new TabHost(realTabActivity);
    }
}
