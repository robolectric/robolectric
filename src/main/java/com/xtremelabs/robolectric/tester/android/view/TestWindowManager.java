package com.xtremelabs.robolectric.tester.android.view;

import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

@SuppressWarnings({"UnusedDeclaration"})
public class TestWindowManager implements WindowManager {

    private Display display;
    private List<View> views = new ArrayList<View>();

    @Override
    public void addView(View view, android.view.ViewGroup.LayoutParams layoutParams) {
        views.add(view);
    }

    @Override
    public void removeView(View view) {
        views.remove(view);
    }

    @Override
    public void updateViewLayout(View arg0, android.view.ViewGroup.LayoutParams arg1) {
    }

    @Override
    public Display getDefaultDisplay() {
        return display == null ? display = newInstanceOf(Display.class) : display;
    }

    @Override
    public void removeViewImmediate(View arg0) {
    }

    public List<View> getViews() {
        return views;
    }
}
