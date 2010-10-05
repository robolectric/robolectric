package com.xtremelabs.droidsugar.fakes;

import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class FakeAdapterView extends FakeView {
    private AdapterView realAdapterView;

    Adapter adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private boolean valid = false;
    private int selectedPosition;

    private List<Object> previousItems = new ArrayList<Object>();

    public FakeAdapterView(AdapterView adapterView) {
        super(adapterView);
        this.realAdapterView = adapterView;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        adapter.registerDataSetObserver(new AdapterViewDataSetObserver());

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
     * Check if our adapter's items have changed without onChanged() or onInvalidated having been called.
     *
     * If the items have changed without notification, an exception will be thrown.
     *
     * @return true if the object is valid, false if not
     */
    public boolean checkValidity() {
        update();
        return valid;
    }

    public int getSelectedItemPosition() {
      return selectedPosition;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    public int getCount() {
        return adapter.getCount();
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        this.onItemSelectedListener = listener;
    }

    public final AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return onItemSelectedListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public final AdapterView.OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public Object getItemAtPosition(int position) {
        Adapter adapter = getAdapter();
        return (adapter == null || position < 0) ? null : adapter.getItem(position);
    }

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
