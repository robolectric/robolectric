package com.xtremelabs.robolectric.fakes;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListView.class)
public class FakeListView extends FakeAdapterView {
    public boolean itemsCanFocus;
    public List<View> headerViews = new ArrayList<View>();
    public List<View> footerViews = new ArrayList<View>();
    private ListView realListView;

    public FakeListView(ListView listView) {
        super(listView);
        this.realListView = listView;
    }

    @Implementation
    public void setItemsCanFocus(boolean itemsCanFocus) {
        this.itemsCanFocus = itemsCanFocus;
    }

    @Implementation
    @Override
    public boolean performItemClick(View view, int position, long id) {
        AdapterView.OnItemClickListener onItemClickListener = getOnItemClickListener();
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(realListView, view, position, id);
            return true;
        }
        return false;
    }

    @Implementation
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Implementation
    public void addHeaderView(View headerView) {
        ensureAdapterNotSet("header");
        headerViews.add(headerView);
    }

    @Implementation
    public void addFooterView(View footerView, Object data, boolean isSelectable) {
        ensureAdapterNotSet("footer");
        footerViews.add(footerView);
    }

    @Implementation
    public void addFooterView(View footerView) {
        addFooterView(footerView, null, false);
    }


    public boolean performItemClick(int position) {
        return realListView.performItemClick(realListView.getChildAt(position), position, realListView.getItemIdAtPosition(position));
    }

    public int findIndexOfItemContainingText(String targetText) {
        for (int i = 0; i < realListView.getChildCount(); i++) {
            View childView = realListView.getChildAt(i);
            String innerText = ((FakeView) ProxyDelegatingHandler.getInstance().proxyFor(childView)).innerText();
            if (innerText.contains(targetText)) {
                return i;
            }
        }
        return -1;
    }

    public View findItemContainingText(String targetText) {
        int itemIndex = findIndexOfItemContainingText(targetText);
        if (itemIndex == -1) {
            return null;
        }
        return realListView.getChildAt(itemIndex);
    }

    public void clickFirstItemContainingText(String targetText) {
        int itemIndex = findIndexOfItemContainingText(targetText);
        if (itemIndex == -1) {
            throw new IllegalArgumentException("No item found containing text \"" + targetText + "\"");
        }
        performItemClick(itemIndex);
    }

    private void ensureAdapterNotSet(String view) {
        if (adapter != null) {
            throw new IllegalStateException("Cannot add " + view + " view to list -- setAdapter has already been called");
        }
    }
}
