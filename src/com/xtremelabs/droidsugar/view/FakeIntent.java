package com.xtremelabs.droidsugar.view;

import android.content.*;
import android.os.*;

import java.io.*;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeIntent {
    private Intent realIntent;

    private Bundle extras;
    public String action;
    public Class<?> componentClass;

    public FakeIntent(Intent realIntent) {
        this.realIntent = realIntent;
    }

    public void __constructor__(Context packageContext, Class cls) {
        componentClass = cls;
    }

    public Intent setAction(String action) {
        this.action = action;
        return realIntent;
    }

    public Bundle getExtras() {
        return extras;
    }

    public void putExtra(String key, long value) {
        init();
        extras.putLong(key, value);
    }

    public void putExtra(String key, Serializable value) {
        init();
        extras.putSerializable(key, value);
    }

    public void putExtra(String key, Parcelable value) {
        init();
        extras.putParcelable(key, value);
    }

    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    private void init() {
        if (extras == null) {
            extras = new Bundle();
        }
    }
}
