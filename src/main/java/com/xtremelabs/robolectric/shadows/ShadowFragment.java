package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Field;

@Implements(Fragment.class)
public class ShadowFragment {
    @RealObject Fragment realFragment;

    protected View view;
    protected FragmentActivity fragmentActivity;
    private String tag;
    private Bundle savedInstanceState;
    private int containerViewId;
    private boolean shouldReplace;
    private Bundle arguments;
    private boolean attached;

    private int fragmentId;

    private Fragment targetFragment;
    private boolean resumed;

    public void setView(View view) {
        this.view = view;
    }

    public void setActivity(FragmentActivity activity) {
        if (fragmentActivity != null) realFragment.onDetach();
        fragmentActivity = activity;
        try {
            Field field = Fragment.class.getDeclaredField("mActivity");
            field.setAccessible(true);
            field.set(realFragment, activity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to set mActivity field");
        }
    }

    @Implementation
    public View getView() {
        return view;
    }

    @Implementation
    public FragmentActivity getActivity() {
        return fragmentActivity;
    }

    @Implementation
    public void startActivity(Intent intent) {
        if (fragmentActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        fragmentActivity.startActivity(intent);
    }

    @Implementation
    public void startActivityForResult(Intent intent, int requestCode) {
        if (fragmentActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        fragmentActivity.startActivityForResult(intent, requestCode);
    }

    @Implementation
    final public FragmentManager getFragmentManager() {
        return fragmentActivity.getSupportFragmentManager();
    }

    @Implementation
    public String getTag() {
        return tag;
    }

    @Implementation
    public Resources getResources() {
        if (fragmentActivity == null) {
            throw new IllegalStateException("Fragment " + this + " not attached to Activity");
        }
        return fragmentActivity.getResources();
    }

    @Implementation
    public String getString(int id) {
        return getResources().getString(id);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSavedInstanceState(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
    }

    public Bundle getSavedInstanceState() {
        return savedInstanceState;
    }

    public void setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
    }

    public int getContainerViewId() {
        return containerViewId;
    }

    public void setShouldReplace(boolean shouldReplace) {
        this.shouldReplace = shouldReplace;
    }

    public boolean getShouldReplace() {
        return shouldReplace;
    }

    @Implementation
    public Bundle getArguments() {
        return arguments;
    }

    @Implementation
    public void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    public void setAttached(boolean isAttached) {
        attached = isAttached;
    }

    public boolean isAttached() {
        return attached;
    }
    
    @Implementation
    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }

    @Implementation
    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    @Implementation
    public int getId() {
        return fragmentId;
    }

    @Implementation
    public boolean isAdded() {
        return fragmentActivity != null;
    }

    @Implementation
    public boolean isVisible() {
        return fragmentActivity != null;
    }

    @Implementation
    public Fragment getTargetFragment() {
        return targetFragment;
    }

    @Implementation
    public void setTargetFragment(Fragment targetFragment, int requestCode) {
        this.targetFragment = targetFragment;
    }

    @Implementation
    public void onResume() {
        this.resumed = true;
    }

    public void resume() {
        realFragment.onResume();
    }

    @Implementation
    public void onPause() {
        this.resumed = false;
    }

    public void pause() {
        realFragment.onPause();
    }

    @Implementation
    public boolean isResumed() {
        return resumed;
    }

    public void createView() {
        final FragmentActivity activity = getActivity();
        view = realFragment.onCreateView(activity.getLayoutInflater(), null, null);
        realFragment.onViewCreated(view, null);
    }

    public void setFragmentId(int fragmentId) {
        this.fragmentId = fragmentId;
    }
}
