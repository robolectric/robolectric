package com.xtremelabs.robolectric.shadows;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.SpinnerAdapter;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ActionBarImpl extends ActionBar {
    private View customView;
    private int options;
    private Context context;

    public ActionBarImpl(Context context) {
        this.context = context;
    }

    @Override
    public Context getThemedContext() {
        return context;
    }

    @Override
    public void setCustomView(View view) {
        customView = view;
    }

    @Override
    public void setCustomView(View view, LayoutParams layoutParams) {
    }

    @Override
    public void setCustomView(int resId) {
    }

    @Override
    public void setIcon(int resId) {
    }

    @Override
    public void setIcon(Drawable icon) {
    }

    @Override
    public void setLogo(int resId) {
    }

    @Override
    public void setLogo(Drawable logo) {
    }

    @Override
    public void setListNavigationCallbacks(SpinnerAdapter adapter, OnNavigationListener callback) {
    }

    @Override
    public void setSelectedNavigationItem(int position) {
    }

    @Override
    public int getSelectedNavigationIndex() {
        return 0;
    }

    @Override
    public int getNavigationItemCount() {
        return 0;
    }

    @Override
    public void setTitle(CharSequence title) {
    }

    @Override
    public void setTitle(int resId) {
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
    }

    @Override
    public void setSubtitle(int resId) {
    }

    @Override
    public void setDisplayOptions(int options) {
        this.options = options;
    }

    @Override
    public void setDisplayOptions(int options, int mask) {
    }

    @Override
    public void setDisplayUseLogoEnabled(boolean useLogo) {
    }

    @Override
    public void setDisplayShowHomeEnabled(boolean showHome) {
    }

    @Override
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
    }

    @Override
    public void setDisplayShowTitleEnabled(boolean showTitle) {
    }

    @Override
    public void setDisplayShowCustomEnabled(boolean showCustom) {
    }

    @Override
    public void setBackgroundDrawable(Drawable d) {
    }

    @Override
    public View getCustomView() {
        return customView;
    }

    @Override
    public CharSequence getTitle() {
        return null;
    }

    @Override
    public CharSequence getSubtitle() {
        return null;
    }

    @Override
    public int getNavigationMode() {
        return 0;
    }

    @Override
    public void setNavigationMode(int mode) {
    }

    @Override
    public int getDisplayOptions() {
        return options;
    }

    @Override
    public Tab newTab() {
        return null;
    }

    @Override
    public void addTab(Tab tab) {
    }

    @Override
    public void addTab(Tab tab, boolean setSelected) {
    }

    @Override
    public void addTab(Tab tab, int position) {
    }

    @Override
    public void addTab(Tab tab, int position, boolean setSelected) {
    }

    @Override
    public void removeTab(Tab tab) {
    }

    @Override
    public void removeTabAt(int position) {
    }

    @Override
    public void removeAllTabs() {
    }

    @Override
    public void selectTab(Tab tab) {
    }

    @Override
    public Tab getSelectedTab() {
        return null;
    }

    @Override
    public Tab getTabAt(int index) {
        return null;
    }

    @Override
    public int getTabCount() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public boolean isShowing() {
        return false;
    }

    @Override
    public void addOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
    }

    @Override
    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener listener) {
    }
}
