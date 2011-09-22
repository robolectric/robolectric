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
public class ShadowListView extends ShadowAbsListView {
    @RealObject private ListView realListView;

    private boolean itemsCanFocus;
    private List<View> headerViews = new ArrayList<View>();
    private List<View> footerViews = new ArrayList<View>();

    @Implementation
    @Override
    public View findViewById(int id) {
        View child = super.findViewById(id);
        if (child == null) {
            child = findView(headerViews, id);

            if (child == null) {
                child = findView(footerViews, id);
            }
        }
        return child;
    }

    private View findView(List<View> views, int viewId) {
        View child = null;
        for (View v : views) {
            child = v.findViewById(viewId);
            if (child != null) {
                break;
            }
        }
        return child;
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
        addHeaderView(headerView, null, true);
    }

    @Implementation
    public void addHeaderView(View headerView, Object data, boolean isSelectable) {
        ensureAdapterNotSet("header");
        headerViews.add(headerView);
        realListView.addView(headerView);
    }

    @Implementation
    public int getHeaderViewsCount() {
        return headerViews.size();
    }

    @Implementation
    public void addFooterView(View footerView, Object data, boolean isSelectable) {
        ensureAdapterNotSet("footer");
        footerViews.add(footerView);
        realListView.addView(footerView);
    }

    @Implementation
    public void addFooterView(View footerView) {
        addFooterView(footerView, null, false);
    }

    @Implementation
    public void removeAllViews() {
        throw new UnsupportedOperationException();
    }

    @Implementation
    public void removeView(View view) {
        throw new UnsupportedOperationException();
    }

    @Implementation
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException();
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
        if (getAdapter() != null) {
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

    @Override
    protected void addViews() {
        for (View headerView : headerViews) {
            addView(headerView);
        }

        super.addViews();

        for (View footerView : footerViews) {
            addView(footerView);
        }
    }
}
