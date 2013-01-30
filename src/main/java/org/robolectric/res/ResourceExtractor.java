package org.robolectric.res;

import org.robolectric.tester.android.util.ResName;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class ResourceExtractor {
    private static final ResourceRemapper RESOURCE_REMAPPER = new ResourceRemapper();
    private static final boolean REMAP_RESOURCES = false;

    private Map<ResName, Integer> resourceNameToId = new HashMap<ResName, Integer>();
    private Map<Integer, ResName> resourceIdToResName = new HashMap<Integer, ResName>();
    private Set<Class> processedRFiles = new HashSet<Class>();
    private Integer maxUsedInt = null;

    public ResourceExtractor() {
    }

    public ResourceExtractor(ResourceExtractor... subExtractors) {
        for (ResourceExtractor subExtractor : subExtractors) {
            HashSet<Class> overlapClasses = new HashSet<Class>(processedRFiles);
            overlapClasses.retainAll(subExtractor.processedRFiles);
            if (!overlapClasses.isEmpty()) {
                throw new RuntimeException("found overlap for " + overlapClasses);
            }
            processedRFiles.addAll(subExtractor.processedRFiles);

            merge(resourceNameToId, subExtractor.resourceNameToId, "resourceNameToId");
            merge(resourceIdToResName, subExtractor.resourceIdToResName, "resourceIdToResName");
        }
    }

    private static <K,V> void merge(Map<K, V> map1, Map<K, V> map2, String name) {
        int expected = map1.size() + map2.size();
        map1.putAll(map2);
        if (map1.size() != expected) {
            throw new IllegalStateException("there must have been some overlap for " + name + "! expected " + expected + " but got " + map1.size());
        }
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
        if (REMAP_RESOURCES) RESOURCE_REMAPPER.remapRClass(rClass);

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

                  if (!section.equals("styleable")) {
                    ResName resName = new ResName(packageName, section, field.getName());

                    resourceNameToId.put(resName, value);

                    if (resourceIdToResName.containsKey(value) && REMAP_RESOURCES) {
                      throw new RuntimeException(
                          value + " is already defined with name: " + resourceIdToResName.get(value) + " can't also call it: " + resName);
                    }

                    resourceIdToResName.put(value, resName);
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
        Integer resourceId = getResourceId(new ResName(fullyQualifiedResourceName));
        // todo warn if resourceId is null
        return resourceId;
    }

    public Integer getResourceId(ResName resName) {
        Integer id = resourceNameToId.get(resName);
        if (id == null && "android".equals(resName.namespace)) {
            if (maxUsedInt == null) {
                maxUsedInt = resourceIdToResName.isEmpty() ? 0 : Collections.max(resourceIdToResName.keySet());
            }
            id = ++maxUsedInt;
            resourceNameToId.put(resName, id);
            resourceIdToResName.put(id, resName);
            System.out.println("INFO: no id mapping found for " + resName.getFullyQualifiedName() + "; assigning " + id);
        }
        return id;
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
        return resourceIdToResName.get(resourceId);
    }
}