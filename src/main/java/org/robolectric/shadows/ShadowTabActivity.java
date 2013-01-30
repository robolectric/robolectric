package org.robolectric.shadows;

import android.app.TabActivity;
import android.widget.TabHost;
import android.widget.TabWidget;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

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
