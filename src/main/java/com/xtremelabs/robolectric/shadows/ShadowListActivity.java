package com.xtremelabs.robolectric.shadows;

import android.app.ListActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Shadow of {@code ListActivity} that supports the retrieval of {@code ListViews}
 */

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListActivity.class)
public class ShadowListActivity extends ShadowActivity {
    private ListView listView;
    private ListAdapter listAdapter;

    @Implementation
    public ListView getListView() {
        if (listView == null) {
            if ((listView = findListView(getContentView())) == null) {
                throw new RuntimeException("No ListView found under content view");
            }
        }
        return listView;
    }

    public void setListView(ListView view) {
    	listView = view;
    }

    @Implementation
    public void setListAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
        ListView lv = findListView(getContentView());
        if (lv != null) {
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        Method handler = realActivity.getClass().getDeclaredMethod("onListItemClick",
                                ListView.class, View.class, int.class, long.class);
                        handler.setAccessible(true);
                        handler.invoke(realActivity, parent, view, position, id);
                    } catch (NoSuchMethodException ignored) {
                    } catch (InvocationTargetException ignored) {
                    } catch (IllegalAccessException ignored) {
                    }
                }
            });
            lv.setAdapter(listAdapter);
        }
    }

    @Implementation
    public ListAdapter getListAdapter() {
        return listAdapter;
    }

    private ListView findListView(View parent) {
        if (parent instanceof ListView) {
            return (ListView) parent;
        } else if (parent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                ListView listView = findListView(viewGroup.getChildAt(i));
                if (listView != null) {
                    return listView;
                }
            }
        }
        return null;
    }
}
