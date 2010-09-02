package com.xtremelabs.droidsugar.fakes;

import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class FakeAdapterView extends FakeView {
    private AdapterView realAdapterView;

    Adapter adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private boolean valid = false;
    private int selectedPosition;

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
            for (int i = 0; i < adapter.getCount(); i++) {
                addView(adapter.getView(i, null, realAdapterView));
            }
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
