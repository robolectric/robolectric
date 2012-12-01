package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ResourceExtractor {
    private Map<ResName, Integer> resourceNameToId = new HashMap<ResName, Integer>();
    private Map<Integer, ResName> resourceIdToFullyQualifiedName = new HashMap<Integer, ResName>();
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
                    int value;
                    try {
                        value = field.getInt(null);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    ResName resName = new ResName(packageName, section, field.getName());

                    if (!section.equals("styleable")) {
                        resourceNameToId.put(resName, value);

                        if (resourceIdToFullyQualifiedName.containsKey(value)) {
                            throw new RuntimeException(value + " is already defined with name: " + resourceIdToFullyQualifiedName.get(value) + " can't also call it: " + resName);
                        }

                        resourceIdToFullyQualifiedName.put(value, resName);
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
        Integer resourceId = resourceNameToId.get(new ResName(fullyQualifiedResourceName));
        // todo warn if resourceId is null
        return resourceId;
    }

    public Integer getResourceId(ResName resName) {
      return resourceNameToId.get(resName);
    }

    public static @NotNull String qualifyResourceName(String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName.contains(":")) {
            return possiblyQualifiedResourceName;
        } else {
            return contextPackageName + ":" + possiblyQualifiedResourceName;
        }
    }

    public String getResourceName(int resourceId) {
        ResName resName = getResName(resourceId);
        return (resName != null) ? resName.getFullyQualifiedName() : null;
    }

    public ResName getResName(int resourceId) {
        return resourceIdToFullyQualifiedName.get(resourceId);
    }
}