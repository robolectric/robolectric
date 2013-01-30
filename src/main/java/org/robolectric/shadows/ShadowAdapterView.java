package com.xtremelabs.robolectric.shadows;

import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class ShadowAdapterView extends ShadowViewGroup {
    private static int ignoreRowsAtEndOfList = 0;
    private static boolean automaticallyUpdateRowViews = true;

    @RealObject
    private AdapterView realAdapterView;

    private Adapter adapter;
    private View mEmptyView;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    private boolean valid = false;
    private int selectedPosition;
    private int itemCount = 0;

    private List<Object> previousItems = new ArrayList<Object>();

    @Implementation
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;

        if (null != adapter) {
            adapter.registerDataSetObserver(new AdapterViewDataSetObserver());
        }

        invalidateAndScheduleUpdate();
        setSelection(0);
    }

    @Implementation
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        updateEmptyStatus(adapter == null || adapter.isEmpty());
    }

    @Implementation
    public int getPositionForView(android.view.View view) {
        while (view.getParent() != null && view.getParent() != realView) {
            view = (View) view.getParent();
        }

        for (int i = 0; i < getChildCount(); i++) {
            if (view == getChildAt(i)) {
                return i;
            }
        }

        return AdapterView.INVALID_POSITION;
    }

    private void invalidateAndScheduleUpdate() {
        valid = false;
        itemCount = adapter == null ? 0 : adapter.getCount();
        if (mEmptyView != null) {
            updateEmptyStatus(itemCount == 0);
        }

        if (hasOnItemSelectedListener() && itemCount == 0) {
            onItemSelectedListener.onNothingSelected(realAdapterView);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!valid) {
                    update();
                    valid = true;
                }
            }
        });
    }

    private boolean hasOnItemSelectedListener() {
        return onItemSelectedListener != null;
    }

    private void updateEmptyStatus(boolean empty) {
        // code taken from the real AdapterView and commented out where not (yet?) applicable

        // we don't deal with filterMode yet...
//        if (isInFilterMode()) {
//            empty = false;
//        }

        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                // If the caller just removed our empty view, make sure the list view is visible
                setVisibility(View.VISIBLE);
            }

            // leave layout for the moment...
//            // We are now GONE, so pending layouts will not be dispatched.
//            // Force one here to make sure that the state of the list matches
//            // the state of the adapter.
//            if (mDataChanged) {
//                this.onLayout(false, mLeft, mTop, mRight, mBottom);
//            }
        } else {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.GONE);
            }
            setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check if our adapter's items have changed without {@code onChanged()} or {@code onInvalidated()} having been called.
     *
     * @return true if the object is valid, false if not
     * @throws RuntimeException if the items have been changed without notification
     */
    public boolean checkValidity() {
        update();
        return valid;
    }

    /**
     * Set to avoid calling getView() on the last row(s) during validation. Useful if you are using a special
     * last row, e.g. one that goes and fetches more list data as soon as it comes into view. This sets a static
     * on the class, so be sure to call it again and set it back to 0 at the end of your test.
     *
     * @param countOfRows The number of rows to ignore at the end of the list.
     * @see com.xtremelabs.robolectric.shadows.ShadowAdapterView#checkValidity()
     */
    public static void ignoreRowsAtEndOfListDuringValidation(int countOfRows) {
        ignoreRowsAtEndOfList = countOfRows;
    }

    /**
     * Use this static method to turn off the feature of this class which calls getView() on all of the
     * adapter's rows in setAdapter() and after notifyDataSetChanged() or notifyDataSetInvalidated() is
     * called on the adapter. This feature is turned on by default. This sets a static on the class, so
     * set it back to true at the end of your test to avoid test pollution.
     *
     * @param shouldUpdate false to turn off the feature, true to turn it back on
     */
    public static void automaticallyUpdateRowViews(boolean shouldUpdate) {
        automaticallyUpdateRowViews = shouldUpdate;
    }

    @Implementation
    public int getSelectedItemPosition() {
        return selectedPosition;
    }

    @Implementation
    public Object getSelectedItem() {
        int pos = getSelectedItemPosition();
        return getItemAtPosition(pos);
    }

    @Implementation
    public Adapter getAdapter() {
        return adapter;
    }

    @Implementation
    public int getCount() {
        return itemCount;
    }

    @Implementation
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    @Implementation
    public final AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return onItemSelectedListener;
    }

    @Implementation
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Implementation
    public final AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    @Implementation
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @Implementation
    public AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return onItemLongClickListener;
    }

    @Implementation
    public Object getItemAtPosition(int position) {
        Adapter adapter = getAdapter();
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

    @Implementation
    public long getItemIdAtPosition(int position) {
        Adapter adapter = getAdapter();
        return (adapter == null || position < 0) ? AdapterView.INVALID_ROW_ID : adapter.getItemId(position);
    }

    @Implementation
    public void setSelection(final int position) {
        selectedPosition = position;

        if (selectedPosition >= 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (hasOnItemSelectedListener()) {
                        onItemSelectedListener.onItemSelected(realAdapterView, getChildAt(position), position, getAdapter().getItemId(position));
                    }
                }
            });
        }
    }

    @Implementation
    public boolean performItemClick(View view, int position, long id) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(realAdapterView, view, position, id);
            return true;
        }
        return false;
    }

    public boolean performItemLongClick(View view, int position, long id) {
        if (onItemLongClickListener != null) {
            onItemLongClickListener.onItemLongClick(realAdapterView, view, position, id);
            return true;
        }
        return false;
    }

    public boolean performItemClick(int position) {
        return realAdapterView.performItemClick(realAdapterView.getChildAt(position),
                position, realAdapterView.getItemIdAtPosition(position));
    }

    public int findIndexOfItemContainingText(String targetText) {
        for (int i = 0; i < realAdapterView.getChildCount(); i++) {
            View childView = realAdapterView.getChildAt(i);
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
        return realAdapterView.getChildAt(itemIndex);
    }

    public void clickFirstItemContainingText(String targetText) {
        int itemIndex = findIndexOfItemContainingText(targetText);
        if (itemIndex == -1) {
            throw new IllegalArgumentException("No item found containing text \"" + targetText + "\"");
        }
        performItemClick(itemIndex);
    }

    @Implementation
    public View getEmptyView() {
        return mEmptyView;
    }

    private void update() {
        if (!automaticallyUpdateRowViews) {
            return;
        }

        super.removeAllViews();
        addViews();
    }

    protected void addViews() {
        Adapter adapter = getAdapter();
        if (adapter != null) {
            if (valid && (previousItems.size() - ignoreRowsAtEndOfList != adapter.getCount() - ignoreRowsAtEndOfList)) {
                throw new ArrayIndexOutOfBoundsException("view is valid but adapter.getCount() has changed from " + previousItems.size() + " to " + adapter.getCount());
            }

            List<Object> newItems = new ArrayList<Object>();
            for (int i = 0; i < adapter.getCount() - ignoreRowsAtEndOfList; i++) {
                View view = adapter.getView(i, null, realAdapterView);
                // don't add null views
                if (view != null) {
                    addView(view);
                }
                newItems.add(adapter.getItem(i));
            }

            if (valid && !newItems.equals(previousItems)) {
                throw new RuntimeException("view is valid but current items <" + newItems + "> don't match previous items <" + previousItems + ">");
            }
            previousItems = newItems;
        }
    }

    /**
     * Simple default implementation of {@code android.database.DataSetObserver}
     */
    protected class AdapterViewDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            invalidateAndScheduleUpdate();
        }

        @Override
        public void onInvalidated() {
            invalidateAndScheduleUpdate();
        }
    }
}
