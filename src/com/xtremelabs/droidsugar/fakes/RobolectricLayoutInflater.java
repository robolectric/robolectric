package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.droidsugar.util.ViewLoader;

public class RobolectricLayoutInflater extends LayoutInflater {
    private final ViewLoader viewLoader;
    private Context context;

    public RobolectricLayoutInflater(ViewLoader viewLoader, Context context) {
        super(context);
        this.viewLoader = viewLoader;
        this.context = context;
    }

    @Override
    public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
        View view = viewLoader.inflateView(context, resource);
        if (root != null && attachToRoot) {
            root.addView(view);
        }
        return view;
    }

    @Override
    public View inflate(int resource, ViewGroup root) {
        return inflate(resource, root, true);
    }

    @Override
    public LayoutInflater cloneInContext(Context context) {
        return this;
    }
}
