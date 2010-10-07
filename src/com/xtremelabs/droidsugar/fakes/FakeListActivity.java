package com.xtremelabs.droidsugar.fakes;

import android.app.ListActivity;
import android.view.View;
import android.widget.ListView;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListActivity.class)
public class FakeListActivity extends FakeActivity {
    private ListView listView;

    public FakeListActivity(ListActivity realActivity) {
        super(realActivity);
    }

    @Implementation
    public ListView getListView() {
        if (listView == null) {
            if ((listView = findListView(contentView)) == null) {
                throw new RuntimeException("No ListView found under content view");
            }
        }
        return listView;
    }

    private ListView findListView(View parent) {
        if (parent instanceof ListView) {
            return (ListView) parent;
        }
        FakeViewGroup proxyView = (FakeViewGroup) ProxyDelegatingHandler.getInstance().proxyFor(parent);
        for (int i = 0; i < proxyView.getChildCount(); i++) {
            ListView listView = findListView(proxyView.getChildAt(i));
            if (listView != null) {
                return listView;
            }
        }
        return null;
    }
}
