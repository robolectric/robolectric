package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

public class ResourceExtractor extends ResourceIndex {
  private static final ResourceRemapper RESOURCE_REMAPPER = new ResourceRemapper();
  private static final boolean REMAP_RESOURCES = false;

  private final Class<?> processedRFile;
  private Integer maxUsedInt = null;

  public ResourceExtractor() {
    processedRFile = null;
  }

  /**
   * Constructs a ResourceExtractor for the Android system resources.
   * @param classLoader
   */
  public ResourceExtractor(ClassLoader classLoader) {
    Class<?> androidRClass;
    try {
      androidRClass = classLoader.loadClass("android.R");
      Class<?> androidInternalRClass = classLoader.loadClass("com.android.internal.R");

      process(androidRClass, "android", true);
      process(androidInternalRClass, "android", false);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    processedRFile = androidRClass;
  }

  public ResourceExtractor(ResourcePath resourcePath) {
    if (REMAP_RESOURCES) RESOURCE_REMAPPER.remapRClass(resourcePath.rClass);
    processedRFile = resourcePath.rClass;
    String packageName = packageNameFor(resourcePath.rClass);
    process(resourcePath.rClass, packageName, true);
  }

  private void process(Class<?> rClass, String packageName, boolean checkForCollisions) {
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

            if (checkForCollisions && resourceIdToResName.containsKey(value)) {
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

  private String packageNameFor(Class<?> rClass) {
    String name = rClass.getCanonicalName();
    if (name == null) {
      throw new RuntimeException("weirdly-named class " + rClass);
    }
    int lastDot = name.lastIndexOf(".");
    if (lastDot < 0) throw new RuntimeException("weirdly-named class " + rClass);
    return name.substring(0, lastDot);
  }

  @Override
  public synchronized Integer getResourceId(ResName resName) {
    Integer id = resourceNameToId.get(resName);
    if (id == null && ("android".equals(resName.packageName) || "".equals(resName.packageName))) {
      if (maxUsedInt == null) {
        maxUsedInt = resourceIdToResName.isEmpty() ? 0 : Collections.max(resourceIdToResName.keySet());
      }
      id = ++maxUsedInt;
      resourceNameToId.put(resName, id);
      resourceIdToResName.put(id, resName);
      System.out.println("INFO: no id mapping found for " + resName.getFullyQualifiedName() + "; assigning ID #0x" + Integer.toHexString(id));
    }
    return id;
  }

  @Override
  public synchronized ResName getResName(int resourceId) {
    return resourceIdToResName.get(resourceId);
  }

  public Class<?> getProcessedRFile() {
    return processedRFile;
  }

  @Override public String toString() {
    return "ResourceExtractor{" +
        "package=" + processedRFile +
        '}';
  }
}