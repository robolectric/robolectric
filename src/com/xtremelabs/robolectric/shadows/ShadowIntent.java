package com.xtremelabs.robolectric.shadows;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.robolectric.ProxyDelegatingHandler;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.Join;
import com.xtremelabs.robolectric.util.SheepWrangler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Intent.class)
public class ShadowIntent {
    @SheepWrangler private ProxyDelegatingHandler proxyDelegatingHandler;
    private Intent realIntent;
    public HashMap<String, Object> extras = new HashMap<String, Object>();
    public String action;
    public ComponentName componentName;
    public Uri data;

    public ShadowIntent(Intent realIntent) {
        this.realIntent = realIntent;
    }

    public void __constructor__(Context packageContext, Class cls) {
        componentName = new ComponentName(packageContext, cls);
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
    public Uri getData() {
        return data;
    }

    @Implementation
    public Intent setClassName(String packageName, String className) {
        componentName = new ComponentName(packageName, className);
        return realIntent;
    }

    @Implementation
    public Intent setData(Uri data) {
        this.data = data;
        return realIntent;
    }

    @Implementation
    public Intent putExtras(Intent src) {
        ShadowIntent srcShadowIntent = (ShadowIntent) proxyDelegatingHandler.shadowFor(src);
        extras = new HashMap<String, Object>(srcShadowIntent.extras);
        return realIntent;
    }

    public ComponentName getComponent() {
        return componentName;
    }

    @Implementation
    public Bundle getExtras() {
        return new Bundle();
    }

    @Implementation
    public Intent putExtra(String key, int value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, long value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, Serializable value) {
        extras.put(key, serializeCycle(value));
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, Parcelable value) {
        extras.put(key, value);
        return realIntent;
    }

    @Implementation
    public Intent putExtra(String key, String value) {
        extras.put(key, value);
        return realIntent;
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

    public boolean realIntentEquals(ShadowIntent o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (action != null ? !action.equals(o.action) : o.action != null) return false;
        if (componentName != null ? !componentName.equals(o.componentName) : o.componentName != null)
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
                        ifWeHave(componentName, "componentName"),
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
