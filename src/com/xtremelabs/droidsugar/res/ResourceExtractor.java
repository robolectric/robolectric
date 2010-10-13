package com.xtremelabs.droidsugar.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ResourceExtractor {
    Map<String, Integer> resourceStringToId = new HashMap<String, Integer>();
    Map<Integer, String> resourceIdToString = new HashMap<Integer, String>();

    public ResourceExtractor() {
    }

    public void addRClass(Class rClass) throws Exception {
        for (Class innerClass : rClass.getClasses()) {
            for (Field field : innerClass.getDeclaredFields()) {
                if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
                    String name = innerClass.getSimpleName() + "/" + field.getName();
                    int value = field.getInt(null);
                    resourceStringToId.put(name, value);
                    resourceIdToString.put(value, name);
                }
            }
        }
    }

    public Map<String, Integer> getResourceStringToId() {
        return resourceStringToId;
    }

    public Map<Integer, String> getResourceIdToString() {
        return resourceIdToString;
    }

    String getResourceName(int resourceId) {
        return getResourceIdToString().get(resourceId);
    }
}