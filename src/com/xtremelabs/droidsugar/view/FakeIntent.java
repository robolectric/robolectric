package com.xtremelabs.droidsugar.view;

import java.io.Serializable;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeIntent {
    private Intent realIntent;
    public HashMap extras = new HashMap();
    public String action;
    public Class<?> componentClass;
    public Uri data;

    public FakeIntent(Intent realIntent) {
        this.realIntent = realIntent;
    }

    public void __constructor__(Context packageContext, Class cls) {
        componentClass = cls;
    }

    public void __constructor__(String action, Uri uri) {
        this.action = action;
        data = uri;
    }

    public Intent setAction(String action) {
        this.action = action;
        return realIntent;
    }

    public String getAction() {
        return action;
    }

    public Intent setData(Uri data) {
        this.data = data;
        return realIntent;
    }

    public Intent putExtras(Intent src) {
        FakeIntent srcFakeIntent = (FakeIntent) ProxyDelegatingHandler.getInstance().proxyFor(src);
        extras = new HashMap(srcFakeIntent.extras);
        return realIntent;
    }

    public Bundle getExtras() {
        return new Bundle();
    }

    public void putExtra(String key, int value) {
        extras.put(key, value);
    }

    public void putExtra(String key, long value) {
        extras.put(key, value);
    }

    public void putExtra(String key, Serializable value) {
        extras.put(key, value);
    }

    public void putExtra(String key, Parcelable value) {
        extras.put(key, value);
    }

    public void putExtra(String key, String value) {
        extras.put(key, value);
    }

    public void putExtra(String key, byte[] value) {
        extras.put(key, value);
    }

    public String getStringExtra(String name) {
        return (String) extras.get(name);
    }

    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    public int getIntExtra(String name, int defaultValue) {
        Integer foundValue = (Integer) extras.get(name);
        return foundValue == null ? defaultValue : foundValue;
    }

    public byte[] getByteArrayExtra(String name) {
        return (byte[]) extras.get(name);
    }

    public boolean realIntentEquals(FakeIntent o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (action != null ? !action.equals(o.action) : o.action != null) return false;
        if (componentClass != null ? !componentClass.equals(o.componentClass) : o.componentClass != null)
            return false;
        if (data != null ? !data.equals(o.data) : o.data != null) return false;
        if (extras != null ? !extras.equals(o.extras) : o.extras != null) return false;

        return true;
    }
}
