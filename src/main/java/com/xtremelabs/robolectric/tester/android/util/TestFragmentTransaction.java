package com.xtremelabs.robolectric.tester.android.util;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class TestFragmentTransaction extends FragmentTransaction {

    private TestFragmentManager fragmentManager;
    private int containerViewId;
    private String tag;
    private Fragment fragment;
    private boolean isReplacing;

    public TestFragmentTransaction(TestFragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public FragmentTransaction add(Fragment fragment, String tag) {
        this.containerViewId = View.NO_ID;
        this.tag = tag;
        this.fragment = fragment;
        return this;
    }

    @Override
    public FragmentTransaction add(int containerViewId, Fragment fragment) {
        this.containerViewId = containerViewId;
        this.fragment = fragment;
        return this;
    }

    @Override
    public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
        this.containerViewId = containerViewId;
        this.tag = tag;
        this.fragment = fragment;
        return this;
    }

    @Override
    public FragmentTransaction replace(int containerViewId, Fragment fragment) {
        this.containerViewId = containerViewId;
        this.fragment = fragment;
        this.isReplacing = true;
        return this;
    }

    @Override
    public FragmentTransaction replace(int containerViewId, Fragment fragment, String tag) {
        this.containerViewId = containerViewId;
        this.tag = tag;
        this.fragment = fragment;
        this.isReplacing = true;
        return this;
    }

    @Override
    public FragmentTransaction remove(Fragment fragment) {
        return null;
    }

    @Override
    public FragmentTransaction hide(Fragment fragment) {
        return null;
    }

    @Override
    public FragmentTransaction show(Fragment fragment) {
        return null;
    }

    @Override
    public FragmentTransaction detach(Fragment fragment) {
        return null;
    }

    @Override
    public FragmentTransaction attach(Fragment fragment) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FragmentTransaction setCustomAnimations(int enter, int exit) {
        return null;
    }

    @Override
    public FragmentTransaction setCustomAnimations(int enter, int exit, int popEnter, int popExit) {
        return null;
    }

    @Override
    public FragmentTransaction setTransition(int transit) {
        return null;
    }

    @Override
    public FragmentTransaction setTransitionStyle(int styleRes) {
        return null;
    }

    @Override
    public FragmentTransaction addToBackStack(String name) {
        return null;
    }

    @Override
    public boolean isAddToBackStackAllowed() {
        return false;
    }

    @Override
    public FragmentTransaction disallowAddToBackStack() {
        return null;
    }

    @Override
    public FragmentTransaction setBreadCrumbTitle(int res) {
        return null;
    }

    @Override
    public FragmentTransaction setBreadCrumbTitle(CharSequence text) {
        return null;
    }

    @Override
    public FragmentTransaction setBreadCrumbShortTitle(int res) {
        return null;
    }

    @Override
    public FragmentTransaction setBreadCrumbShortTitle(CharSequence text) {
        return null;
    }

    @Override
    public int commit() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                fragmentManager.addFragment(containerViewId, tag, fragment, isReplacing);
                fragmentManager.startFragment(fragment);
            }
        });
        return 0;
    }

    @Override
    public int commitAllowingStateLoss() {
        return 0;
    }
}
