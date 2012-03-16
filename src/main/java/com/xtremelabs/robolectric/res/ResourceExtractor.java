package com.xtremelabs.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ResourceExtractor {
    private Map<String, Integer> localResourceStringToId = new HashMap<String, Integer>();
    private Map<String, Integer> systemResourceStringToId = new HashMap<String, Integer>();
    private Map<Integer, String> resourceIdToString = new HashMap<Integer, String>();

    public void addLocalRClass(Class rClass) throws Exception {
        addRClass(rClass, false);
    }

    public void addSystemRClass(Class rClass) throws Exception {
        addRClass(rClass, true);
    }

    private void addRClass(Class rClass, boolean isSystemRClass) throws Exception {
        for (Class innerClass : rClass.getClasses()) {
            for (Field field : innerClass.getDeclaredFields()) {
                if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
                    String section = innerClass.getSimpleName();
                    String name = section + "/" + field.getName();
                    int value = field.getInt(null);

                    if (isSystemRClass) {
                        name = "android:" + name;
                    }

                    if (!section.equals("styleable")) {
                        if (isSystemRClass) {
                            systemResourceStringToId.put(name, value);
                        } else {
                            localResourceStringToId.put(name, value);
                        }

                        if (resourceIdToString.containsKey(value)) {
                            throw new RuntimeException(value + " is already defined with name: " + resourceIdToString.get(value) + " can't also call it: " + name);
                        }
                        resourceIdToString.put(value, name);
                    }
                }
            }
        }
    }

    public Integer getResourceId(String resourceName) {
        if (resourceName.contains("android:")) { // namespace needed for platform files
            return getResourceId(resourceName, true);
        } else {
            return getResourceId(resourceName, false);
        }
    }

    public Integer getLocalResourceId(String value) {
        boolean isSystem = false;
        return getResourceId(value, isSystem);
    }

    public Integer getResourceId(String resourceName, boolean isSystemResource) {
        if (resourceName == null ) {
            return null;
        }
        if (resourceName.equals("@null")) {
        	return 0;
        }
        
        if (resourceName.startsWith("@+id")) {
            resourceName = resourceName.substring(2);
        } else if (resourceName.startsWith("@+android:id")) {
            resourceName = resourceName.substring(2);
        } else if (resourceName.startsWith("@")) {
            resourceName = resourceName.substring(1);
        }

        if (isSystemResource) {
            return systemResourceStringToId.get(resourceName);
        } else {
            return localResourceStringToId.get(resourceName);
        }
    }

    public String getResourceName(int resourceId) {
        return resourceIdToString.get(resourceId);
    }
}