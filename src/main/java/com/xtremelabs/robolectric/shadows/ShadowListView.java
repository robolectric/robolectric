package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListView.class)
public class ShadowListView extends ShadowAdapterView {
    @RealObject private ListView realListView;

    private boolean itemsCanFocus;
    private List<View> headerViews = new ArrayList<View>();
    private List<View> footerViews = new ArrayList<View>();

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
    public void addHeaderView(View headerView, Object data, boolean isSelectable ) {
        ensureAdapterNotSet("header");
        headerViews.add(headerView);
    }
    
    @Implementation
    public int getHeaderViewsCount() {
    	return headerViews.size();
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
            String innerText = shadowOf(childView).innerText();
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

    public boolean isItemsCanFocus() {
        return itemsCanFocus;
    }

    public List<View> getHeaderViews() {
        return headerViews;
    }

    public void setHeaderViews(List<View> headerViews) {
        this.headerViews = headerViews;
    }

    public List<View> getFooterViews() {
        return footerViews;
    }

    public void setFooterViews(List<View> footerViews) {
        this.footerViews = footerViews;
    }
}
