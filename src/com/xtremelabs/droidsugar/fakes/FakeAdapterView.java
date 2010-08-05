package com.xtremelabs.droidsugar.fakes;

import android.widget.Adapter;
import android.widget.AdapterView;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AdapterView.class)
public class FakeAdapterView extends FakeView {
    private AdapterView realAdapterView;

    private Adapter adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    public FakeAdapterView(AdapterView adapterView) {
        super(adapterView);
        this.realAdapterView = adapterView;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }

    public Adapter getAdapter() {
        return adapter;
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

    public void setSelection(int position) {
        if (onItemSelectedListener != null) {
            onItemSelectedListener.onItemSelected(realAdapterView, null, position, -1);
        }
    }
}
