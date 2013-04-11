package org.robolectric.shadows;

import android.util.SparseArray;
import android.util.SparseBooleanArray;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import static org.robolectric.Robolectric.shadowOf;

@Implements(SparseBooleanArray.class)
public class ShadowSparseBooleanArray {
    private SparseArray<Boolean> sparseArray = new SparseArray<Boolean>();

    @RealObject
    private SparseBooleanArray realObject;

    @Implementation
    public boolean get(int key) {
        return sparseArray.get(key);
    }

    @Implementation
    public boolean get(int key, boolean valueIfKeyNotFound) {
        return sparseArray.get(key, valueIfKeyNotFound);
    }

    @Implementation
    public void delete(int key) {
        sparseArray.delete(key);
    }

    @Implementation
    public void put(int key, boolean value) {
        sparseArray.put(key, value);
    }

    @Implementation
    public int size() {
        return sparseArray.size();
    }

    @Implementation
    public int keyAt(int index) {
        return sparseArray.keyAt(index);
    }

    @Implementation
    public boolean valueAt(int index) {
        return sparseArray.valueAt(index);
    }

    @Implementation
    public int indexOfKey(int key) {
        return sparseArray.indexOfKey(key);
    }

    @Implementation
    public int indexOfValue(boolean value) {
        return sparseArray.indexOfValue(value);
    }

    @Implementation
    public void clear() {
        sparseArray.clear();
    }

    @Implementation
    public void append(int key, boolean value) {
        sparseArray.append(key, value);
    }

    @Implementation
    @Override
    public int hashCode() {
        return sparseArray.hashCode();
    }

    @Implementation
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != realObject.getClass())
            return false;

        ShadowSparseBooleanArray target = shadowOf((SparseBooleanArray) o);
        return sparseArray.equals(target.sparseArray);
    }
}
