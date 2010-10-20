package com.xtremelabs.robolectric.fakes;

import android.app.ListActivity;
import android.view.View;
import android.widget.ListView;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.SheepWrangler;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListActivity.class)
public class FakeListActivity extends FakeActivity {
    @SheepWrangler private ProxyDelegatingHandler proxyDelegatingHandler;
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
        FakeViewGroup proxyView = (FakeViewGroup) proxyDelegatingHandler.proxyFor(parent);
        for (int i = 0; i < proxyView.getChildCount(); i++) {
            ListView listView = findListView(proxyView.getChildAt(i));
            if (listView != null) {
                return listView;
            }
        }
        return null;
    }
}
