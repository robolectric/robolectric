package com.xtremelabs.droidsugar.view;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ALL"})
public class FakeView {
    private View realView;

    private int id;
    private List<View> children = new ArrayList<View>();
    private Context context;
    private int visibility;
    public boolean selected;

    public FakeView(View view) {
        this.realView = view;
    }

    public void __constructor__(Context context) {
        this.context = context;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public View findViewById(int id) {
        if (id == this.id) {
            return realView;
        }

        for (View child : children) {
            View found = child.findViewById(id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void addView(View child) {
        children.add(child);
    }

    public int getChildCount() {
        return children.size();
    }

    public View getChildAt(int index) {
        return children.get(index);
    }

    public final Context getContext() {
        return context;
    }

    public Resources getResources() {
        return context.getResources();
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
