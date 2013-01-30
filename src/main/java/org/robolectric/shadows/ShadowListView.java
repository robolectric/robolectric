package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.List;

import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListView.class)
public class ShadowListView extends ShadowAbsListView {
    @RealObject private ListView realListView;

    private boolean itemsCanFocus;
    private List<View> headerViews = new ArrayList<View>();
    private List<View> footerViews = new ArrayList<View>();

    private int choiceMode;
    private SparseBooleanArray checkedItemPositions = new SparseBooleanArray();

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
        boolean handled = false;
        if (choiceMode != ListView.CHOICE_MODE_NONE) {
            handled = true;

            if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
                boolean newValue = !checkedItemPositions.get(position, false);
                checkedItemPositions.put(position, newValue);
            } else {
                boolean newValue = !checkedItemPositions.get(position, false);
                if (newValue) {
                	checkedItemPositions.clear();
                	checkedItemPositions.put(position, true);
                }
            }
        }

        handled |= super.performItemClick(view, position, id);
        return handled;
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
    public int getFooterViewsCount() {
        return footerViews.size();
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

    @Implementation
    public int getChoiceMode() {
        return choiceMode;
    }

    @Implementation
    public void setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
    }

    @Implementation
    public void setItemChecked(int position, boolean value) {
        if (choiceMode == ListView.CHOICE_MODE_SINGLE) {
            checkedItemPositions.clear();
            checkedItemPositions.put(position, value);
        } else if (choiceMode == ListView.CHOICE_MODE_MULTIPLE) {
            checkedItemPositions.put(position, value);
        }
    }

    @Implementation
    public int getCheckedItemPosition() {
        if (choiceMode != ListView.CHOICE_MODE_SINGLE || checkedItemPositions.size() != 1)
            return ListView.INVALID_POSITION;

        return checkedItemPositions.keyAt(0);
    }

    @Implementation
    public SparseBooleanArray getCheckedItemPositions() {
        if (choiceMode == ListView.CHOICE_MODE_NONE)
            return null;

        return checkedItemPositions;
    }
}
