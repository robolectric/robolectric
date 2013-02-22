package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResourceExtractor extends ResourceIndex {
    private static final ResourceRemapper RESOURCE_REMAPPER = new ResourceRemapper();
    private static final boolean REMAP_RESOURCES = false;

    private Set<Class> processedRFiles = new HashSet<Class>();
    private Integer maxUsedInt = null;

    public ResourceExtractor() {
    }

    public ResourceExtractor(ResourcePath resourcePath) {
        addRClass(resourcePath.rClass);
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
    public synchronized Integer getResourceId(ResName resName) {
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
    public synchronized ResName getResName(int resourceId) {
        return resourceIdToResName.get(resourceId);
    }
}