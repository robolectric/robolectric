package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import com.xtremelabs.droidsugar.util.FakeHelper;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Activity.class)
public class FakeActivity extends FakeContextWrapper {
    private Intent intent;
    public View contentView;

    public boolean finishWasCalled;
    public int resultCode;
    public Intent resultIntent;
    public Activity parent;
    private Activity realActivity;
    private TestWindow window;

    public FakeActivity(Activity realActivity) {
        super(realActivity);
        this.realActivity = realActivity;
    }

    public final Application getApplication() {
        return FakeHelper.application;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setContentView(int layoutResID) {
        contentView = resourceLoader.viewLoader.inflateView(realActivity, layoutResID);
    }

    public void setContentView(View view) {
        contentView = view;
    }

    public final void setResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public final void setResult(int resultCode, Intent data) {
        this.resultCode = resultCode;
        resultIntent = data;
    }

    public LayoutInflater getLayoutInflater() {
        return new RobolectricLayoutInflater(resourceLoader.viewLoader, realActivity);
    }

    public View findViewById(int id) {
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            throw new RuntimeException("you should have called setContentView() first");
        }
    }

    public final Activity getParent() {
        return parent;
    }

    public void finish() {
        finishWasCalled = true;
    }

    public Window getWindow() {
        if(window == null) {
            window = new TestWindow(realActivity);
        }
        return window;
    }

}
