package org.robolectric.shadows;

import android.util.SparseBooleanArray;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import java.util.HashMap;
import java.util.Map;

@Implements(value = SparseBooleanArray.class, callThroughByDefault = true)
public class ShadowSparseBooleanArray {
    @RealObject private SparseBooleanArray realObject;

    @Implementation
    @Override
    public int hashCode() {
        return mapFor(realObject).hashCode();
    }

    @Implementation
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != SparseBooleanArray.class)
            return false;

        SparseBooleanArray target = (SparseBooleanArray) o;
        if (realObject.size() != target.size()) return false;

        return (mapFor(realObject).equals(mapFor(target)));
    }

    private Map<Integer, Boolean> mapFor(SparseBooleanArray sparseBooleanArray) {
        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>();
        for (int i = 0; i < sparseBooleanArray.size(); i++) {
            map.put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(1));
        }
        return map;
    }
}
