package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.lang.reflect.Field;

/**
 * Shadow class for "Fragment".  Note that this is for the support package v4 version of "Fragment", not the android.app
 * one.
 */
@Implements(Fragment.class)
public class ShadowFragment {
    @RealObject
    Fragment realFragment;

    private Bundle arguments;
    private FragmentActivity fragmentActivity;
    private View view;

    @Implementation
    public void setArguments(Bundle bundle) {
        arguments = bundle;
    }

    @Implementation
    public Bundle getArguments() {
        return arguments;
    }

    @Implementation
    public FragmentActivity getActivity() {
        return fragmentActivity;
    }

    @Implementation
    public Resources getResources() {
        return getActivity().getResources();
    }

    @Implementation
    public final CharSequence getText(int resId) {
        return getResources().getText(resId);
    }

    @Implementation
    public final String getString(int resId) {
        return getResources().getString(resId);
    }

    @Implementation
    public final String getString(int resId, Object... formatArgs) {
        return getResources().getString(resId, formatArgs);
    }

    @Implementation
    public View getView() {
        return view;
    }

    @Implementation
    public boolean isAdded() {
        return fragmentActivity != null;
    }

    @Implementation
    public boolean isVisible() {
        return fragmentActivity != null;
    }

    public void setActivity(FragmentActivity activity) {
        fragmentActivity = activity;
        try {
            Field field = Fragment.class.getDeclaredField("mActivity");
            field.setAccessible(true);
            field.set(realFragment, activity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to set mActivity field");
        }
    }

    public void createView() {
        realFragment.onCreate(null);
        final FragmentActivity activity = getActivity();
        view = realFragment.onCreateView(activity.getLayoutInflater(), null, null);
    }
}
