package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.ResName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class ResBundle<T> {
    private final Map<ResName, Values<T>> valuesMap = new HashMap<ResName, Values<T>>();

    public void put(String attrType, String name, T value, XmlLoader.XmlContext xmlContext) {
        ResName resName = new ResName(xmlContext.packageName, attrType, name);

        Values<T> values = valuesMap.get(resName);
        if (values == null) valuesMap.put(resName, values = new Values<T>());
        values.add(new Value<T>(xmlContext.getQualifiers(), value, xmlContext));
        Collections.sort(values);
    }

    public T get(ResName resName, String qualifiers) {
        Value<T> value = getValue(resName, qualifiers);
        return value == null ? null : value.value;
    }

    public Value<T> getValue(ResName resName, String qualifiers) {
        Values<T> values = valuesMap.get(resName);
        return (values != null) ? pick(values, qualifiers) : null;
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
        final XmlLoader.XmlContext xmlContext;

        Value(String qualifiers, T value, XmlLoader.XmlContext xmlContext) {
            if (value == null) {
                throw new NullPointerException();
            }

            this.xmlContext = xmlContext;
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
