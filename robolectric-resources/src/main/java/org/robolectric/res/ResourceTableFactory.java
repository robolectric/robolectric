package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ResourceTableFactory {

  public static ResourceTable newResourceTable(String packageName, Class<?>... rClasses) {
    ResourceTable resourceTable = new ResourceTable(packageName);

    for (Class<?> rClass : rClasses) {
      if (rClass != null) {
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

    return resourceTable;
  }
}