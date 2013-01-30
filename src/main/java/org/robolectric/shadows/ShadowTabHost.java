package com.xtremelabs.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabHost.class)
public class ShadowTabHost extends ShadowFrameLayout {
    private List<TabHost.TabSpec> tabSpecs = new ArrayList<TabHost.TabSpec>();
    private TabHost.OnTabChangeListener listener;
    private int currentTab = -1;

    @RealObject
    TabHost realObject;

    @Implementation
    public android.widget.TabHost.TabSpec newTabSpec(java.lang.String tag) {
        TabSpec realTabSpec = Robolectric.newInstanceOf(TabHost.TabSpec.class);
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
        currentTab = index;
        if (listener != null) {
            listener.onTabChanged(getCurrentTabTag());
        }
    }

    @Implementation
    public void setCurrentTabByTag(String tag) {
        for (int x = 0; x < tabSpecs.size(); x++) {
            TabSpec tabSpec = tabSpecs.get(x);
            if (tabSpec.getTag().equals(tag)) {
                currentTab = x;
            }
        }
        if (listener != null) {
            listener.onTabChanged(getCurrentTabTag());
        }
    }

    @Implementation
    public int getCurrentTab() {
        if (currentTab == -1 && tabSpecs.size() > 0) currentTab = 0;
        return currentTab;
    }

    public TabSpec getCurrentTabSpec() {
        return tabSpecs.get(getCurrentTab());
    }

    @Implementation
    public String getCurrentTabTag() {
        int i = getCurrentTab();
        if (i >= 0 && i < tabSpecs.size()) {
            return tabSpecs.get(i).getTag();
        }
        return null;
    }

    @Implementation
    public void setOnTabChangedListener(android.widget.TabHost.OnTabChangeListener listener) {
        this.listener = listener;
    }

    @Implementation
    public View getCurrentView() {
        ShadowTabSpec ts = Robolectric.shadowOf(getCurrentTabSpec());
        View v = ts.getContentView();
        if (v == null) {
            int viewId = ts.getContentViewId();
            if (getContext() instanceof Activity) {
                v = ((Activity) getContext()).findViewById(viewId);
            } else {
                return null;
            }
        }
        return v;
    }

    @Implementation
    public TabWidget getTabWidget() {
        if (context instanceof Activity) {
            return (TabWidget) ((Activity)context).findViewById(R.id.tabs);
        } else {
            return null;
        }
    }

    public TabHost.TabSpec getSpecByTag(String tag) {
        for (TabHost.TabSpec tabSpec : tabSpecs) {
            if (tag.equals(tabSpec.getTag())) {
                return tabSpec;
            }
        }
        return null;
    }
}
