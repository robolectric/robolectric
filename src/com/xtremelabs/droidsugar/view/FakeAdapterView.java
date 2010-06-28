package com.xtremelabs.droidsugar.view;

import android.widget.Adapter;
import android.widget.AdapterView;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeAdapterView extends FakeView {
    private Adapter adapter;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    public FakeAdapterView(AdapterView adapterView) {
        super(adapterView);
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
}
