package com.xtremelabs.robolectric.fakes;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.SheepWrangler;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Activity.class)
public class FakeActivity extends FakeContextWrapper {
    @SheepWrangler ProxyDelegatingHandler proxyDelegatingHandler;

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

    @Implementation
    public final Application getApplication() {
        return Robolectric.application;
    }

    @Implementation @Override
    public final Application getApplicationContext() {
        return getApplication();
    }

    @Implementation
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Implementation
    public Intent getIntent() {
        return intent;
    }

    @Implementation
    public void setContentView(int layoutResID) {
        contentView = getLayoutInflater().inflate(layoutResID, null);
    }

    @Implementation
    public void setContentView(View view) {
        contentView = view;
    }

    @Implementation
    public final void setResult(int resultCode) {
        this.resultCode = resultCode;
    }

    @Implementation
    public final void setResult(int resultCode, Intent data) {
        this.resultCode = resultCode;
        resultIntent = data;
    }

    @Implementation
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(realActivity);
    }

    @Implementation
    public View findViewById(int id) {
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            throw new RuntimeException("you should have called setContentView() first");
        }
    }

    @Implementation
    public final Activity getParent() {
        return parent;
    }

    @Implementation
    public void finish() {
        finishWasCalled = true;
    }

    @Implementation
    public Window getWindow() {
        if(window == null) {
            window = new TestWindow(realActivity);
        }
        return window;
    }

    @Implementation
    public void onDestroy() {
        assertNoBroadcastListenersRegistered();
    }

    public void assertNoBroadcastListenersRegistered() {
        ((FakeApplication) proxyDelegatingHandler.proxyFor(getApplicationContext())).assertNoBroadcastListenersRegistered(realActivity);
    }
}
