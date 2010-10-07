package com.xtremelabs.droidsugar.fakes;

import android.os.Bundle;
import android.os.Parcelable;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bundle.class)
public class FakeBundle {
    Map<String, Object> map = new HashMap<String, Object>();

    @Implementation
    public void clear() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakeBundle that = (FakeBundle) o;

        if (map != null ? !map.equals(that.map) : that.map != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }
}
