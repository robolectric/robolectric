package com.xtremelabs.droidsugar.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeIntent {
    private Intent realIntent;
    public HashMap extras = new HashMap();
    public String action;
    public Class<?> componentClass;
    public String componentPackageName;
    public Uri data;

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

    public String getAction() {
        return action;
    }

    public Intent setClassName(String packageName, String className) {
        this.componentPackageName = packageName;
        try {
            componentClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return realIntent;
    }

    public Intent setData(Uri data) {
        this.data = data;
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
        verifySerializable(value);
        extras.put(key, value);
    }

    public void putExtra(String key, Parcelable value) {
        extras.put(key, value);
    }

    public void putExtra(String key, String value) {
        extras.put(key, value);
    }

    public boolean hasExtra(String name) {
        return extras.containsKey(name);
    }

    public int getIntExtra(String name, int defaultValue) {
        Integer foundValue = (Integer) extras.get(name);
        return foundValue == null ? defaultValue : foundValue;
    }

    public Serializable getSerializableExtra(String name) {
        return (Serializable) extras.get(name);
    }

    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    private void verifySerializable (Serializable serializable) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(new ByteArrayOutputStream());
            output.writeObject(serializable);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
