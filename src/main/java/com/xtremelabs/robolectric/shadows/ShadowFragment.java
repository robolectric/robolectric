package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Fragment.class)
public class ShadowFragment {
    private View view;
    private FragmentActivity activity;

    public void setView(View view) {
        this.view = view;
    }

    @Implementation
    public View getView() {
        return view;
    }

    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    @Implementation
    public FragmentActivity getActivity() {
        return activity;
    }

    @Implementation
    public void startActivity(Intent intent) {
        activity.startActivity(intent);
    }

    @Implementation
    public void startActivityForResult(Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
    }
}
