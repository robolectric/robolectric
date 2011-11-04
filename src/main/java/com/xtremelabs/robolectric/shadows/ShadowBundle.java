package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bundle.class)
public class ShadowBundle {
    Map<String, Object> map = new HashMap<String, Object>();

    @Implementation
    public Object get(String key) {
        return map.get(key);
    }

    @Implementation
    public void putString(String key, String value) {
        map.put(key, value);
    }

    @Implementation
    public String getString(String key) {
        return (String) map.get(key);
    }

    @Implementation
    public void putLong(String key, long value) {
        map.put(key, value);
    }

    @Implementation
    public long getLong(String key) {
        Object value = map.get(key);
        return value == null ? 0 : (Long) value;
    }
    
    @Implementation
    public long getLong(String key,long defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : (Long) value;
    }
    
    @Implementation
    public void putInt(String key, int value) {
        map.put(key, value);
    }

    @Implementation
    public int getInt(String key) {
        Object value = map.get(key);
        return value == null ? 0 : (Integer) value;
    }
    
    @Implementation
    public int getInt(String key, int defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : (Integer) value;
    }
    
    @Implementation
    public void putDouble(String key, double value) {
        map.put(key, value);
    }

    @Implementation
    public double getDouble(String key) {
        Object value = map.get(key);
        return value == null ? 0 : (Double) value;
    }
    
    @Implementation
    public double getDouble(String key, double defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : (Double) value;
    }
    
    @Implementation
    public void putBoolean(String key, boolean value) {
        map.put(key, value);
    }

    @Implementation
    public boolean getBoolean(String key) {
        Object value = map.get(key);
        return value == null ? false : (Boolean) value;
    }
    
    @Implementation
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = map.get(key);
        return value == null ? defaultValue : (Boolean) value;
    }
    
    @Implementation
    public void putSerializable(String key, Serializable value) {
        map.put(key, value);
    }

    @Implementation
    public Serializable getSerializable(String key) {
        return (Serializable) map.get(key);
    }

    @Implementation
    public void putParcelable(String key, Parcelable value) {
        map.put(key, value);
    }
    
    @Implementation
    public void putParcelableArrayList(String key, ArrayList<? extends Parcelable> value) {
        map.put(key, value);
    }

    @Implementation
    public Parcelable getParcelable(String key) {
        return (Parcelable) map.get(key);
    }
    
    @Implementation
    public ArrayList<Parcelable> getParcelableArrayList(String key) {
    	return (ArrayList<Parcelable>)map.get(key);
    }

    @Implementation
    public ArrayList<String> getStringArrayList(String key) {
        return (ArrayList<String>) map.get(key);
    }

    @Implementation
    public void putAll(Bundle bundle) {
    	map.putAll(((ShadowBundle) Robolectric.shadowOf_(bundle)).map);
    }

    @Implementation
    public void putStringArray(String key, String[] value) {
        map.put(key, value);
    }

    @Implementation
    public String[] getStringArray(String key) {
        return (String[]) map.get(key);
    }

    @Implementation
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Implementation
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Implementation
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override @Implementation
    public boolean equals(Object o) {
        if (o == null) return false;
        o = shadowOf_(o);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowBundle that = (ShadowBundle) o;

        if (map != null ? !map.equals(that.map) : that.map != null) return false;

        return true;
    }

    @Override @Implementation
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
