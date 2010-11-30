package com.xtremelabs.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ResourceExtractor {
    private Map<String, Integer> resourceStringToId = new HashMap<String, Integer>();
    private Map<Integer, String> resourceIdToString = new HashMap<Integer, String>();

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

    public int resourceIdOrZero(String resourceName) {
        Integer resId = getResourceId(resourceName);
        return (resId == null) ? 0 : resId;
    }

    public Integer getResourceId(String resourceName) {
        if (resourceName == null) {
            return null;
        }
        if (resourceName.startsWith("@")) {
            resourceName = resourceName.substring(1);
        }
        return resourceStringToId.get(resourceName);
    }

    public String getResourceName(int resourceId) {
        return resourceIdToString.get(resourceId);
    }
}