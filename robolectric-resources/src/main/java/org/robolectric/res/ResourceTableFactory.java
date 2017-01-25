package org.robolectric.res;

import org.robolectric.util.Logger;

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

      parseResourceFiles(resourcePath, resourceTable);

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
      parseResourceFiles(resourcePath, resourceTable);
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

  private static void parseResourceFiles(ResourcePath resourcePath, PackageResourceTable resourceTable) {
    if (!resourcePath.hasResources()) {
      Logger.debug("No resources for %s", resourceTable.getPackageName());
      return;
    }

    Logger.debug("Loading resources for %s from %s...", resourceTable.getPackageName(), resourcePath.getResourceBase());

    DocumentLoader documentLoader = new DocumentLoader(resourceTable.getPackageName(), resourcePath);

    try {
      documentLoader.load("values",
          new ValueResourceLoader(resourceTable, "/resources/bool", "bool", ResType.BOOLEAN),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='bool']", "bool", ResType.BOOLEAN),
          new ValueResourceLoader(resourceTable, "/resources/color", "color", ResType.COLOR),
          new ValueResourceLoader(resourceTable, "/resources/drawable", "drawable", ResType.DRAWABLE),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='color']", "color", ResType.COLOR),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='drawable']", "drawable", ResType.DRAWABLE),
          new ValueResourceLoader(resourceTable, "/resources/dimen", "dimen", ResType.DIMEN),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='dimen']", "dimen", ResType.DIMEN),
          new ValueResourceLoader(resourceTable, "/resources/integer", "integer", ResType.INTEGER),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='integer']", "integer", ResType.INTEGER),
          new ValueResourceLoader(resourceTable, "/resources/integer-array", "array", ResType.INTEGER_ARRAY),
          new ValueResourceLoader(resourceTable, "/resources/fraction", "fraction", ResType.FRACTION),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='fraction']", "fraction", ResType.FRACTION),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='layout']", "layout", ResType.LAYOUT),
          new PluralResourceLoader(resourceTable),
          new ValueResourceLoader(resourceTable, "/resources/string", "string", ResType.CHAR_SEQUENCE),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='string']", "string", ResType.CHAR_SEQUENCE),
          new ValueResourceLoader(resourceTable, "/resources/string-array", "array", ResType.CHAR_SEQUENCE_ARRAY),
          new ValueResourceLoader(resourceTable, "/resources/array", "array", ResType.TYPED_ARRAY),
          new ValueResourceLoader(resourceTable, "/resources/id", "id", ResType.CHAR_SEQUENCE),
          new ValueResourceLoader(resourceTable, "/resources/item[@type='id']", "id", ResType.CHAR_SEQUENCE),
          new AttrResourceLoader(resourceTable),
          new StyleResourceLoader(resourceTable)
      );

      documentLoader.load("layout", new OpaqueFileLoader(resourceTable, "layout"));
      documentLoader.load("menu", new OpaqueFileLoader(resourceTable, "menu"));
      documentLoader.load("drawable", new OpaqueFileLoader(resourceTable, "drawable", ResType.DRAWABLE));
      documentLoader.load("anim", new OpaqueFileLoader(resourceTable, "anim"));
      documentLoader.load("animator", new OpaqueFileLoader(resourceTable, "animator"));
      documentLoader.load("color", new ColorResourceLoader(resourceTable));
      documentLoader.load("xml", new OpaqueFileLoader(resourceTable, "xml"));
      documentLoader.load("transition", new OpaqueFileLoader(resourceTable, "transition"));
      documentLoader.load("interpolator", new OpaqueFileLoader(resourceTable, "interpolator"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    new DrawableResourceLoader(resourceTable).findDrawableResources(resourcePath);
    new RawResourceLoader(resourcePath).loadTo(resourceTable);
  }
}