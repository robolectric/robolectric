package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResourceExtractor extends ResourceIndex {
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
                    String fieldName = field.getName();
                    ResName resName = new ResName(packageName, section, fieldName);

                    if (section.equals("id") && fieldName.equals("abs__content") || fieldName.equals("gone")) {
                      System.out.println(resName + " -> " + Integer.toHexString(value));
                    }

                    resourceNameToId.put(resName, value);

                    if (resourceIdToResName.containsKey(value)) {
                      String message =
                          value + " is already defined with name: " + resourceIdToResName.get(
                              value) + " can't also call it: " + resName;
                      if (REMAP_RESOURCES) {
                        throw new RuntimeException(message);
                      } else {
                        System.err.println(message);
                      }
                    }

                    resourceIdToResName.put(value, resName);
                  }
                }
            }
        }
    }

    @Override
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

    @Override
    public ResName getResName(int resourceId) {
        return resourceIdToResName.get(resourceId);
    }
}