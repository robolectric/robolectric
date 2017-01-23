package org.robolectric.res;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ResourceTableFactory {

  /**
   * Builds an Android framework resource table in the "android" package space.
   */
  public static PackageResourceTable newFrameworkResourceTable(ResourcePath resourcePath) {
    PackageResourceTable resourceTable = new PackageResourceTable("android");

      if (resourcePath.getRClass() != null) {
        addRClassValues(resourceTable, resourcePath.getRClass());
        addMissingStyleableAttributes(resourceTable, resourcePath.getRClass());
      }
      if (resourcePath.getInternalRClass() != null) {
        addRClassValues(resourceTable, resourcePath.getInternalRClass());
        addMissingStyleableAttributes(resourceTable, resourcePath.getInternalRClass());
      }

      ResourceParser.load(resourcePath, resourceTable);

    return resourceTable;
  }

  /**
   * Creates an application resource table which can be constructed with multiple resources paths representing
   * overlayed resource libraries.
   */
  public static PackageResourceTable newResourceTable(String packageName, ResourcePath... resourcePaths) {
    PackageResourceTable resourceTable = new PackageResourceTable(packageName);

    for (ResourcePath resourcePath : resourcePaths) {
      if (resourcePath.getRClass() != null) {
        addRClassValues(resourceTable, resourcePath.getRClass());
      }
    }

    for (ResourcePath resourcePath : resourcePaths) {
      ResourceParser.load(resourcePath, resourceTable);
    }

    return resourceTable;
  }

  private static void addRClassValues(PackageResourceTable resourceTable, Class<?> rClass) {
    for (Class innerClass : rClass.getClasses()) {
      String resourceType = innerClass.getSimpleName();
      if (!resourceType.equals("styleable")) {
        for (Field field : innerClass.getDeclaredFields()) {
          if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
            int id;
            try {
              id = field.getInt(null);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }

            String resourceName = field.getName();
            resourceTable.addResource(id, resourceType, resourceName);
          }
        }
      }
    }
  }

  /**
   * Check the stylable elements. Not for aapt generated R files but for framework R files it is possible to
   * have attributes in the styleable array for which there is no corresponding R.attr field.
   */
  private static void addMissingStyleableAttributes(PackageResourceTable resourceTable, Class<?> rClass) {
    for (Class innerClass : rClass.getClasses()) {
      if (innerClass.getSimpleName().equals("styleable")) {
        String styleableName = null; // Current styleable name
        int[] styleableArray = null; // Current styleable value array or references
        for (Field field : innerClass.getDeclaredFields()) {
          if (field.getType().equals(int[].class) && Modifier.isStatic(field.getModifiers())) {
            styleableName = field.getName();
            try {
              styleableArray = (int[]) (field.get(null));
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          } else if (field.getType().equals(Integer.TYPE) && Modifier.isStatic(field.getModifiers())) {
            String attributeName = field.getName().substring(styleableName.length() + 1);
            try {
              int styleableIndex = field.getInt(null);
              int attributeResId = styleableArray[styleableIndex];
              resourceTable.addResource(attributeResId, "attr", attributeName);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }
        }
      }
    }
  }
}