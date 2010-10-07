package com.xtremelabs.droidsugar.fakes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.droidsugar.ProxyDelegatingHandler;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;
import com.xtremelabs.droidsugar.util.Join;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Intent.class)
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

    public void __constructor__(String action, Uri uri) {
        this.action = action;
        data = uri;
    }

    public void __constructor__(String action) {
        __constructor__(action, null);
    }

    @Implementation
    public Intent setAction(String action) {
        this.action = action;
        return realIntent;
    }

    @Implementation
    public String getAction() {
        return action;
    }

    @Implementation
    public Intent setClassName(String packageName, String className) {
        this.componentPackageName = packageName;
        try {
            componentClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return realIntent;
    }

    @Implementation
    public Intent setData(Uri data) {
        this.data = data;
        return realIntent;
    }

    @Implementation
    public Intent putExtras(Intent src) {
        FakeIntent srcFakeIntent = (FakeIntent) ProxyDelegatingHandler.getInstance().proxyFor(src);
        extras = new HashMap(srcFakeIntent.extras);
        return realIntent;
    }

    @Implementation
    public Bundle getExtras() {
        return new Bundle();
    }

    @Implementation
    public void putExtra(String key, int value) {
        extras.put(key, value);
    }

    @Implementation
    public void putExtra(String key, long value) {
        extras.put(key, value);
    }

    @Implementation
    public void putExtra(String key, Serializable value) {
        extras.put(key, serializeCycle(value));
    }

    @Implementation
    public void putExtra(String key, Parcelable value) {
        extras.put(key, value);
    }

    @Implementation
    public void putExtra(String key, String value) {
        extras.put(key, value);
    }

    @Implementation
    public boolean hasExtra(String name) {
        return extras.containsKey(name);
    }

    @Implementation
    public void putExtra(String key, byte[] value) {
        extras.put(key, value);
    }

    @Implementation
    public String getStringExtra(String name) {
        return (String) extras.get(name);
    }

    @Implementation
    public Parcelable getParcelableExtra(String name) {
        return (Parcelable) extras.get(name);
    }

    @Implementation
    public int getIntExtra(String name, int defaultValue) {
        Integer foundValue = (Integer) extras.get(name);
        return foundValue == null ? defaultValue : foundValue;
    }

    @Implementation
    public byte[] getByteArrayExtra(String name) {
        return (byte[]) extras.get(name);
    }

    @Implementation
    public Serializable getSerializableExtra(String name) {
        return (Serializable) extras.get(name);
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

    private Serializable serializeCycle(Serializable serializable) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(byteArrayOutputStream);
            output.writeObject(serializable);
            output.close();

            byte[] bytes = byteArrayOutputStream.toByteArray();
            ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (Serializable) input.readObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Intent{" +
                Join.join(
                        ", ",
                        ifWeHave(componentPackageName, "componentPackageName"),
                        ifWeHave(componentClass, "componentClass"),
                        ifWeHave(action, "action"),
                        ifWeHave(extras, "extras"),
                        ifWeHave(data, "data")
                ) +
                '}';
    }

    private String ifWeHave(Object o, String name) {
        if (o == null) return null;
        if (o instanceof Map && ((Map)o).isEmpty()) return null;
        return name + "=" + o;
    }
}
