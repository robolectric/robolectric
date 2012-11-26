package com.xtremelabs.robolectric.res;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ResourceExtractor {
    private Map<String, Integer> resourceNameToId = new HashMap<String, Integer>();
    private Map<Integer, String> resourceIdToFullyQualifiedName = new HashMap<Integer, String>();
    private Set<Class> processedRFiles = new HashSet<Class>();

    public ResourceExtractor() {
    }

    public ResourceExtractor(ResourcePath... resourcePaths) {
        for (ResourcePath resourcePath : resourcePaths) {
            addRClass(resourcePath.rClass);
        }
    }

    public ResourceExtractor(List<ResourcePath> resourcePaths) {
        for (ResourcePath resourcePath : resourcePaths) {
            addRClass(resourcePath.rClass);
        }
    }

    private void addRClass(Class<?> rClass) {
        if (!processedRFiles.add(rClass)) {
            System.out.println("WARN: already extracted resources for " + rClass.getPackage().getName() + ", skipping. You should probably fix this.");
            return;
        }
        String packageName = rClass.getPackage().getName();

        for (Class innerClass : rClass.getClasses()) {
            for (Field field : innerClass.getDeclaredFields()) {
                if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
                    String section = innerClass.getSimpleName();
                    String name = section + "/" + field.getName();
                    int value;
                    try {
                        value = field.getInt(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    String fullyQualifiedName = packageName + ":" + name;

                    if (!section.equals("styleable")) {
                        resourceNameToId.put(fullyQualifiedName, value);

                        if (resourceIdToFullyQualifiedName.containsKey(value)) {
                            throw new RuntimeException(value + " is already defined with name: " + resourceIdToFullyQualifiedName.get(value) + " can't also call it: " + fullyQualifiedName);
                        }

                        resourceIdToFullyQualifiedName.put(value, fullyQualifiedName);
                    }
                }
            }
        }
    }

    public Integer getResourceId(String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName == null ) {
            return null;
        }

        if (possiblyQualifiedResourceName.equals("@null")) {
            return 0;
        }

        String fullyQualifiedResourceName = qualifyResourceName(possiblyQualifiedResourceName, contextPackageName);

        fullyQualifiedResourceName = fullyQualifiedResourceName.replaceAll("[@+]", "");
        Integer resourceId = resourceNameToId.get(fullyQualifiedResourceName);
        // todo warn if resourceId is null
        return resourceId;

    }

    public static @NotNull String qualifyResourceName(String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName.contains(":")) {
            return possiblyQualifiedResourceName;
        } else {
            return contextPackageName + ":" + possiblyQualifiedResourceName;
        }
    }

    public String getResourceName(int resourceId) {
        return resourceIdToFullyQualifiedName.get(resourceId);
    }

    public String getFullyQualifiedResourceName(int resourceId) {
        return getResourceName(resourceId);
    }
}