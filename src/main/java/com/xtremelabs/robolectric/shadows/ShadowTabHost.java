package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.TabHost;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@Implements(TabHost.class)
public class ShadowTabHost extends ShadowFrameLayout {
    private List<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
    private TabHost.OnTabChangeListener listener;
    private TabHost.TabSpec currentTab;

    @RealObject TabHost realObject;

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

    @Implementation
    public void addTab(android.widget.TabHost.TabSpec tabSpec) {
        tabSpecs.add(tabSpec);
        View indicatorAsView = shadowOf(tabSpec).getIndicatorAsView();
        if (indicatorAsView != null) {
            realObject.addView(indicatorAsView);
        }
    }

    @Implementation
    public void setCurrentTab(int index) {
        currentTab = tabSpecs.get(index);
        if (listener != null) {
            listener.onTabChanged(currentTab.getTag());
        }
    }

    @Implementation
    public void setCurrentTabByTag(String tag) {
        for (TabHost.TabSpec tabSpec : tabSpecs) {
            if (tag.equals(tabSpec.getTag())) {
                currentTab = tabSpec;
            }
        }
        if (listener != null) {
            listener.onTabChanged(currentTab.getTag());
        }
    }

    @Implementation
    public void setOnTabChangedListener(android.widget.TabHost.OnTabChangeListener listener) {
        this.listener = listener;
    }
}
