package com.xtremelabs.robolectric.tester.android.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class TestFragmentManager extends FragmentManager {
    private Map<Integer, Fragment> fragmentsById = new HashMap<Integer, Fragment>();
    private Map<String, Fragment> fragmentsByTag = new HashMap<String, Fragment>();
    private FragmentActivity activity;

    public TestFragmentManager(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public FragmentTransaction beginTransaction() {
        return new TestFragmentTransaction(this);
    }

    @Override
    public boolean executePendingTransactions() {
        return false;
    }

    @Override
    public Fragment findFragmentById(int id) {
        return fragmentsById.get(id);
    }

    @Override
    public Fragment findFragmentByTag(String tag) {
        return fragmentsByTag.get(tag);
    }

    @Override
    public void popBackStack() {
    }

    @Override
    public boolean popBackStackImmediate() {
        return false;
    }

    @Override
    public void popBackStack(String name, int flags) {
    }

    @Override
    public boolean popBackStackImmediate(String name, int flags) {
        return false;
    }

    @Override
    public void popBackStack(int id, int flags) {
    }

    @Override
    public boolean popBackStackImmediate(int id, int flags) {
        return false;
    }

    @Override
    public int getBackStackEntryCount() {
        return 0;
    }

    @Override
    public BackStackEntry getBackStackEntryAt(int index) {
        return null;
    }

    @Override
    public void addOnBackStackChangedListener(OnBackStackChangedListener listener) {
    }

    @Override
    public void removeOnBackStackChangedListener(OnBackStackChangedListener listener) {
    }

    @Override
    public void putFragment(Bundle bundle, String key, Fragment fragment) {
    }

    @Override
    public Fragment getFragment(Bundle bundle, String key) {
        return null;
    }

    @Override
    public Fragment.SavedState saveFragmentInstanceState(Fragment f) {
        return null;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
    }

    public void addFragment(int containerViewId, String tag, Fragment fragment, boolean replace) {
        fragmentsById.put(containerViewId, fragment);
        fragmentsByTag.put(tag, fragment);

        shadowOf(fragment).setActivity(activity);
        fragment.onAttach(activity);
        fragment.onCreate(null);

        ViewGroup container = null;
        if (shadowOf(activity).getContentView() != null) {
            container = (ViewGroup) activity.findViewById(containerViewId);
        }

        View view = fragment.onCreateView(activity.getLayoutInflater(), container, null);
        shadowOf(fragment).setView(view);

        fragment.onViewCreated(view, null);
        if (container != null) {
            if (replace) {
                container.removeAllViews();
            }
            container.addView(view);
        }

        fragment.onActivityCreated(null);
    }
}
