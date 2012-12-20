package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.ResName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ResBundle<T> {
    private final Map<String, Values<T>> valuesMap = new HashMap<String, Values<T>>();

    public void put(String name, String qualifiers, T value, XmlLoader.XmlContext xmlContext) {
        name = xmlContext.packageName + ":" + name;

        Values<T> values = valuesMap.get(name);
        if (values == null) valuesMap.put(name, values = new Values<T>());
        values.add(new Value<T>(qualifiers, value));
        Collections.sort(values);
    }

    public T get(String name, String qualifiers) {
        ResName resName = new ResName(name);
        Values<T> values = valuesMap.get(resName.namespace + ":" + resName.name);
        if (values != null) {
            return pick(values, qualifiers).value;
        }
        return null;
    }

    public static <T> Value<T> pick(Values<T> values, String qualifiers) {
        final int count = values.size();
        if (count >= Long.SIZE) throw new RuntimeException("really, more than " + Long.SIZE + " qualifiers?!?");
        long possibles = 0;
        for (int i = 0; i < count; i++) possibles |= 1 << i;

        String[] qualifierList = qualifiers.split("-");
        for (String qualifier : qualifierList) {
            String paddedQualifier = "-" + qualifier + "-";
            long matches = 0;

            for (int i = 0; i < count; i++) {
                if ((possibles & (1 << i)) == 0) continue;

                if (values.get(i).qualifiers.contains(paddedQualifier)) {
                    matches |= 1 << i;
                }
            }

            if (matches != 0) {
                possibles &= matches; // eliminate any that didn't match this qualifier
            }

            if (Long.bitCount(matches) == 1) break;
        }

        for (int i = 0; i < count; i++) {
            if ((possibles & (1 << i)) != 0) return values.get(i);
        }
        throw new IllegalStateException();
    }

    static class Value<T> implements Comparable<Value<T>> {
        final String qualifiers;
        final T value;

        Value(String qualifiers, T value) {
            this.qualifiers = qualifiers == null ? "--" : "-" + qualifiers + "-";
            this.value = value;
        }

        @Override
        public int compareTo(Value<T> o) {
            return qualifiers.compareTo(o.qualifiers);
        }
    }

    static class Values<T> extends ArrayList<Value<T>> {
    }

}
