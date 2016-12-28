package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ResourceTableFactory {

  public static PackageResourceTable newResourceTable(String packageName, ResourcePath... resourcePaths) {
    PackageResourceTable resourceTable = new PackageResourceTable(packageName);

    for (ResourcePath resourcePath : resourcePaths) {
      if (resourcePath.getRClass() != null) {
        addRClassValues(resourceTable, resourcePath.getRClass());
      }
      if (resourcePath.getInternalRClass() != null) {
        addRClassValues(resourceTable, resourcePath.getInternalRClass());
      }
    }

    for (ResourcePath resourcePath : resourcePaths) {
      ResourceParser.load(packageName, resourcePath, resourceTable);
    }

    return resourceTable;
  }

  private static void addRClassValues(ResourceTable resourceTable, Class<?> rClass) {
    for (Class innerClass : rClass.getClasses()) {
      for (Field field : innerClass.getDeclaredFields()) {
        if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
          String resourceType = innerClass.getSimpleName();
          int id;
          try {
            id = field.getInt(null);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }

          if (!resourceType.equals("styleable")) {
            String resourceName = field.getName();
            resourceTable.addResource(id, resourceType, resourceName);
          }
        }
      }
    }
  }
}