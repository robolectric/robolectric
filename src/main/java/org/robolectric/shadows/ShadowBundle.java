package org.robolectric.shadows;

import android.os.Bundle;
import android.os.Parcelable;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.robolectric.Robolectric.shadowOf_;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Bundle.class)
public class ShadowBundle {
    Map<String, Object> map = new HashMap<String, Object>();

    public void __constructor__(Bundle b) {
        putAll(b);
    }

    @Implementation
    public void clear() {
        map.clear();
    }

    @Implementation
    public void remove(String key) {
        map.remove(key);
    }

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
        Object value = map.get(key);
        return value == null || !(value instanceof String) ? null : (String) value;
    }

    @Implementation
    public void putLong(String key, long value) {
        map.put(key, value);
    }

    @Implementation
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Implementation
    public long getLong(String key, long defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Long) ? defaultValue : (Long) value;
    }

    @Implementation
    public void putInt(String key, int value) {
        map.put(key, value);
    }

    @Implementation
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Implementation
    public int getInt(String key, int defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Integer) ? defaultValue : (Integer) value;
    }

    @Implementation
    public void putDouble(String key, double value) {
        map.put(key, value);
    }

    @Implementation
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Implementation
    public double getDouble(String key, double defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Double) ? defaultValue : (Double) value;
    }

    @Implementation
    public void putBoolean(String key, boolean value) {
        map.put(key, value);
    }

    @Implementation
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Implementation
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Boolean) ? defaultValue : (Boolean) value;
    }

    @Implementation
    public void putChar(String key, char value) {
        map.put(key, value);
    }

    @Implementation
    public char getChar(String key) {
        return getChar(key, (char) 0);
    }

    @Implementation
    public char getChar(String key, char defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Character) ? defaultValue : (Character) value;
    }

    @Implementation
    public void putCharSequence(String key, CharSequence value) {
        map.put(key, value);
    }

    @Implementation
    public CharSequence getCharSequence(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof CharSequence) ? null : (CharSequence) value;
    }

    @Implementation
    public void putFloat(String key, float value) {
        map.put(key, value);
    }

    @Implementation
    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    @Implementation
    public float getFloat(String key, float defaultValue) {
        Object value = map.get(key);
        return value == null || !(value instanceof Float) ? defaultValue : (Float) value;
    }

    @Implementation
    public void putSerializable(String key, Serializable value) {
        map.put(key, value);
    }

    @Implementation
    public Serializable getSerializable(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof Serializable) ? null : (Serializable) value;
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
        Object value = map.get(key);
        return value == null || !(value instanceof Parcelable) ? null : (Parcelable) value;
    }

    @Implementation
    public ArrayList<Parcelable> getParcelableArrayList(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof ArrayList) ? null : (ArrayList<Parcelable>) value;
    }

    @Implementation
    public Parcelable[] getParcelableArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof Parcelable[]) ? null : (Parcelable[]) value;
    }

    @Implementation
    public void putParcelableArray(String key, Parcelable[] value) {
        map.put(key, value);
    }

    @Implementation
    public void putStringArrayList(String key, ArrayList<String> value) {
        map.put(key, value);
    }

    @Implementation
    public ArrayList<String> getStringArrayList(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof ArrayList) ? null : (ArrayList<String>) value;
    }

    @Implementation
    public void putCharSequenceArrayList(String key, ArrayList<CharSequence> value) {
        map.put(key, value);
    }

    @Implementation
    public ArrayList<CharSequence> getCharSequenceArrayList(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof ArrayList) ? null : (ArrayList<CharSequence>) value;
    }

    @Implementation
    public void putIntegerArrayList(String key, ArrayList<Integer> value) {
        map.put(key, value);
    }

    @Implementation
    public ArrayList<Integer> getIntegerArrayList(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof ArrayList) ? null : (ArrayList<Integer>) value;
    }

    @Implementation
    public void putBundle(String key, Bundle value) {
        map.put(key, value);
    }

    @Implementation
    public Bundle getBundle(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof Bundle) ? null : (Bundle) value;
    }

    @Implementation
    public void putBooleanArray(String key, boolean[] value) {
        map.put(key, value);
    }

    @Implementation
    public boolean[] getBooleanArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof boolean[]) ? null : (boolean[]) value;
    }

    @Implementation
    public void putByteArray(String key, byte[] value) {
        map.put(key, value);
    }

    @Implementation
    public byte[] getByteArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof byte[]) ? null : (byte[]) value;
    }

    @Implementation
    public void putCharArray(String key, char[] value) {
        map.put(key, value);
    }

    @Implementation
    public char[] getCharArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof char[]) ? null : (char[]) value;
    }

    @Implementation
    public void putDoubleArray(String key, double[] value) {
        map.put(key, value);
    }

    @Implementation
    public double[] getDoubleArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof double[]) ? null : (double[]) value;
    }

    @Implementation
    public void putFloatArray(String key, float[] value) {
        map.put(key, value);
    }

    @Implementation
    public float[] getFloatArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof float[]) ? null : (float[]) value;
    }

    @Implementation
    public void putIntArray(String key, int[] value) {
        map.put(key, value);
    }

    @Implementation
    public int[] getIntArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof int[]) ? null : (int[]) value;
    }

    @Implementation
    public void putLongArray(String key, long[] value) {
        map.put(key, value);
    }

    @Implementation
    public long[] getLongArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof long[]) ? null : (long[]) value;
    }

    @Implementation
    public void putShortArray(String key, short[] value) {
        map.put(key, value);
    }

    @Implementation
    public short[] getShortArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof short[]) ? null : (short[]) value;
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
        Object value = map.get(key);
        return value == null || !(value instanceof String[]) ? null : (String[]) value;
    }

    @Implementation
    public void putCharSequenceArray(String key, CharSequence[] value) {
        map.put(key, value);
    }

    @Implementation
    public CharSequence[] getCharSequenceArray(String key) {
        Object value = map.get(key);
        return value == null || !(value instanceof CharSequence[]) ? null : (CharSequence[]) value;
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

    @Implementation
    public int size() {
        return map.size();
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

    @Override @Implementation
    public String toString() {
        return map.toString();
    }
}
