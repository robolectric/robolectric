package com.xtremelabs.droidsugar.view;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeActivity extends FakeContextWrapper {
    private Intent intent;
    public View contentView;

    public boolean finishWasCalled;
    public Intent startActivityIntent;
    public int resultCode;
    public Intent resultIntent;

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setContentView(int layoutResID) {
        contentView = viewLoader.inflateView(null, layoutResID);
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
        return new FakeLayoutInflater(viewLoader);
    }

    public View findViewById(int id) {
        if (contentView != null) {
            return contentView.findViewById(id);
        } else {
            return getLayoutInflater().inflate(id, null);
        }
    }

    public void startActivity(Intent intent) {
        startActivityIntent = intent;
    }

    public void finish() {
        finishWasCalled = true;
    }
}
