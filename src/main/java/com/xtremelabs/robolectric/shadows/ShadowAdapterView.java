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

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class ShadowAdapterView extends ShadowViewGroup {
    @RealObject private AdapterView realAdapterView;

    Adapter adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private boolean valid = false;
    private int selectedPosition;

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

    private void invalidateAndScheduleUpdate() {
        valid = false;
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
     * Non-Android accessor.
     *
     * @return the index of the selected item
     */
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
        return adapter.getCount();
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
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (onItemSelectedListener != null) {
                    onItemSelectedListener.onItemSelected(realAdapterView, getChildAt(position), position, getAdapter().getItemId(position));
                }
            }
        });
    }

    @Implementation
    public boolean performItemClick(View view, int position, long id) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(realAdapterView, view, position, id);
            return true;
        }
        return false;
    }

    private void update() {
        removeAllViews();

        Adapter adapter = getAdapter();
        if (adapter != null) {
            if (valid && previousItems.size() != adapter.getCount()) {
                throw new ArrayIndexOutOfBoundsException("view is valid but adapter.getCount() has changed from " + previousItems.size() + " to " + adapter.getCount());
            }

            List<Object> newItems = new ArrayList<Object>();
            for (int i = 0; i < adapter.getCount(); i++) {
                newItems.add(adapter.getItem(i));
                addView(adapter.getView(i, null, realAdapterView));
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
