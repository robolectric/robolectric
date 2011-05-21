package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.Serializable;
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
    public Parcelable getParcelable(String key) {
        return (Parcelable) map.get(key);
    }

    @Implementation
    public boolean containsKey(String key) {
        return map.containsKey(key);
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
