package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.xtremelabs.robolectric.Robolectric;

public class TestFragment extends Fragment {

    public static final int FRAGMENT_VIEW_ID = 2323;
    public boolean onAttachWasCalled;
    public boolean onCreateWasCalled;
    public boolean onCreateViewWasCalled;
    public boolean onActivityCreatedWasCalled;
    public LayoutInflater onCreateViewInflater;
    public View onCreateViewReturnValue;
    public boolean onStartWasCalled;
    public boolean onResumeWasCalled;
    public Activity onAttachActivity;

    @Override
    public void onAttach(Activity activity) {
        onAttachWasCalled = true;
        onAttachActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        onCreateWasCalled = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        onCreateViewWasCalled = true;
        onCreateViewInflater = inflater;
        onCreateViewReturnValue = new View(Robolectric.application.getApplicationContext());
        onCreateViewReturnValue.setId(FRAGMENT_VIEW_ID);
        return onCreateViewReturnValue;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onActivityCreatedWasCalled = true;
    }

    @Override
    public void onStart() {
        onStartWasCalled = true;
    }

    @Override
    public void onResume() {
        onResumeWasCalled = true;
    }
}
