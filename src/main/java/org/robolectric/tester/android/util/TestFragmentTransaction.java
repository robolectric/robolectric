package com.xtremelabs.robolectric.tester.android.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

public class TestFragmentTransaction extends FragmentTransaction {

    private TestFragmentManager fragmentManager;
    private int containerViewId;
    private String tag;
    private Fragment fragment;
    private boolean replacing;
    private boolean starting;
    private boolean removing;
    private boolean addedToBackStack;
    private String backStackName;
    private int lastEnterAnimation;
    private int lastExitAnimation;
    private Fragment fragmentToRemove;
    private boolean committedAllowingStateLoss;
    private Fragment fragmentToAttach;

    public TestFragmentTransaction(TestFragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public FragmentTransaction add(Fragment fragment, String tag) {
        return add(View.NO_ID, fragment, tag);
    }

    @Override
    public FragmentTransaction add(int containerViewId, Fragment fragment) {
        return add(containerViewId, fragment, null);
    }

    @Override
    public FragmentTransaction add(int containerViewId, Fragment fragment, String tag) {
        this.containerViewId = containerViewId;
        this.tag = tag;
        this.fragment = fragment;
        this.starting = true;
        return this;
    }

    @Override
    public FragmentTransaction replace(int containerViewId, Fragment fragment) {
        return replace(containerViewId, fragment, null);
    }

    @Override
    public FragmentTransaction replace(int containerViewId, Fragment fragment, String tag) {
        this.containerViewId = containerViewId;
        this.tag = tag;
        this.fragment = fragment;
        this.replacing = true;
        this.starting = true;
        return this;
    }

    @Override
    public FragmentTransaction remove(Fragment fragment) {
        this.fragmentToRemove = fragment;
        this.removing = true;
        return this;
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
        fragmentToAttach = fragment;
        return this;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public FragmentTransaction setCustomAnimations(int enter, int exit) {
        this.lastEnterAnimation = enter;
        this.lastExitAnimation = exit;
        return this;
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
        backStackName = name;
        addedToBackStack = true;
        return this;
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
        fragmentManager.commitLater(this);
        return 0;
    }

    @Override
    public int commitAllowingStateLoss() {
        committedAllowingStateLoss = true;
        return commit();
    }

    public boolean isAddedToBackStack() {
        return addedToBackStack;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public String getTag() {
        return tag;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public boolean isReplacing() {
        return replacing;
    }

    public boolean isStarting() {
        return starting;
    }

    public boolean isRemoving() {
        return removing;
    }

    public String getBackStackName() {
        return backStackName;
    }

    public int getLastEnterAnimation() {
        return lastEnterAnimation;
    }

    public int getLastExitAnimation() {
        return lastExitAnimation;
    }

    public TestFragmentManager getManager() {
        return fragmentManager;
    }

    public Fragment getFragmentToRemove() {
        return fragmentToRemove;
    }

    public boolean isCommittedAllowingStateLoss() {
        return committedAllowingStateLoss;
    }

    public boolean isAttaching() {
        return fragmentToAttach != null;
    }

    public Fragment getFragmentToAttach() {
        return fragmentToAttach;
    }
}
