package com.xtremelabs.droidsugar.view;

import android.content.*;
import android.view.*;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeActivity extends FakeContextWrapper {
    private Intent intent;
    private View contentView;

    public boolean finishWasCalled;
    public Intent startActivityIntent;

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setContentView(int layoutResID) {
        contentView = viewLoader.inflateView(null, layoutResID);
    }

    public View findViewById(int id) {
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            return null;
        }
    }

    public void startActivity(Intent intent) {
        startActivityIntent = intent;
    }

    public void finish() {
        finishWasCalled = true;
    }
}
