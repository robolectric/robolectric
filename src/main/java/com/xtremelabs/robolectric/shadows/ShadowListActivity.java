package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import android.app.ListActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadow of {@code ListActivity} that supports the retrieval of {@code ListViews}
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListActivity.class)
public class ShadowListActivity extends ShadowActivity {
    private ListView listView;

    @Implementation
    public ListView getListView() {
        if (listView == null) {
            if ((listView = findListView(contentView)) == null) {
                throw new RuntimeException("No ListView found under content view");
            }
        }
        return listView;
    }
    
    @Implementation
    public  void setListAdapter(final android.widget.ListAdapter adapter) {
        synchronized (this) {
            getListView();
            listView.setAdapter(adapter);
        }
        
    }

    public void setListView(final ListView view) {
    	listView = view;
    }
    
    private ListView findListView(final View parent) {
        if (parent instanceof ListView) {
            return (ListView) parent;
        }
        //If the parent isn't a ViewGroup we should check other elements
        if (!(parent instanceof ViewGroup)) {
            return null;
        }
        ShadowViewGroup shadowViewGroup = (ShadowViewGroup) shadowOf(parent);
        for (int i = 0; i < shadowViewGroup.getChildCount(); i++) {
            ListView listView = findListView(shadowViewGroup.getChildAt(i));
            if (listView != null) {
                return listView;
            }
        }
        return null;
    }
}
