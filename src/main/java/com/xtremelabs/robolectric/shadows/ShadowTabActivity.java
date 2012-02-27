package com.xtremelabs.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabWidget;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabActivity.class)
public class ShadowTabActivity extends ShadowActivityGroup {

    @RealObject TabActivity realTabActivity;
    TabHost tabhost;
    @Implementation
    public TabHost getTabHost() {
    	if (tabhost==null) {
    		tabhost = new TabHost(realTabActivity);
    	}
        return tabhost;
    }

    @Implementation
    public TabWidget getTabWidget() {
        return getTabHost().getTabWidget();
    }
}
