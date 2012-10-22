package com.xtremelabs.robolectric.tester.android.view;

import android.view.View;
import android.view.ContextMenu;
import android.graphics.drawable.Drawable;
import android.content.Context;

public class TestContextMenu extends TestMenu implements ContextMenu {

    public TestContextMenu() {
        this(null);
    }

    public TestContextMenu(Context context) {
        super(context);
    }

    @Override
    public ContextMenu setHeaderTitle(int id) {
        return this;
    }

    @Override
    public ContextMenu setHeaderTitle(CharSequence charSequence) {
        return this;
    }

    @Override
    public ContextMenu setHeaderIcon(int id) {
        return this;
    }

    @Override
    public ContextMenu setHeaderIcon(Drawable drawable) {
        return this;
    }

    @Override
    public ContextMenu setHeaderView(View view) {
        return this;
    }

    @Override
    public void clearHeader() {
    }
}
