package org.robolectric.res;

import org.robolectric.util.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceTableFactory {
  /**
   * Builds an Android framework resource table in the "android" package space.
   */
  public PackageResourceTable newFrameworkResourceTable(final ResourcePath resourcePath) {
    return cache("android", new ResourcePath[] {resourcePath}, new Builder() {
      @Override
      public PackageResourceTable build() {
        InMemoryPackageResourceTable resourceTable = new InMemoryPackageResourceTable("android");

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
    });
  }

  /**
   * Creates an application resource table which can be constructed with multiple resources paths representing
   * overlayed resource libraries.
   */
  public PackageResourceTable newResourceTable(final String packageName, final ResourcePath... resourcePaths) {
    return cache(packageName, resourcePaths, new Builder() {
      @Override
      public PackageResourceTable build() {
        InMemoryPackageResourceTable resourceTable = new InMemoryPackageResourceTable(packageName);

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
    });
  }

  private PackageResourceTable cache(String packageName, ResourcePath[] resourcePaths, Builder builder) {
    Path cacheDirPath = FileSystems.getDefault().getPath(System.getProperty("user.home"), ".robolectric", "cache", "resource-tables");
    Path resCachePath = cacheDirPath.resolve(packageName + "-" + hash(resourcePaths) + ".res");
    ResStore resStore = new ResStore();
    try {
      if (Files.exists(resCachePath)) {
        System.out.println("Read " + packageName + " resources from " + resCachePath + " -- " + (Files.size(resCachePath) / 1024 / 1024) + "mb");
        return resStore.load(resCachePath);
      } else {
        PackageResourceTable packageResourceTable = builder.build();
        Files.createDirectories(resCachePath.getParent());
        resStore.save(packageResourceTable, resCachePath);
        System.out.println("Wrote " + packageName + " resources to " + resCachePath + " -- " + (Files.size(resCachePath) / 1024 / 1024) + "mb");
        return packageResourceTable;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String hash(ResourcePath[] resourcePaths) {
    long hash = 7;
    for (ResourcePath resourcePath : resourcePaths) {
      FsFile resourceBase = resourcePath.getResourceBase();
      String path = resourceBase == null ? "" : resourceBase.getPath();

      File file = new File(path);
      if (!file.isAbsolute()) {
        path = file.getAbsolutePath();
      }

      int length = path.length();
      for (int i = 0; i < length; i++) {
        hash = hash * 31 + path.charAt(i);
      }
    }
    return Long.toHexString(hash);
  }

  interface Builder {
    PackageResourceTable build();
  }

  private void addRClassValues(InMemoryPackageResourceTable resourceTable, Class<?> rClass) {
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
  private void addMissingStyleableAttributes(InMemoryPackageResourceTable resourceTable, Class<?> rClass) {
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

  private void parseResourceFiles(ResourcePath resourcePath, InMemoryPackageResourceTable resourceTable) {
    if (!resourcePath.hasResources()) {
      Logger.debug("No resources for %s", resourceTable.getPackageName());
      return;
    }

    Logger.debug("Loading resources for %s from %s...", resourceTable.getPackageName(), resourcePath.getResourceBase());

    try {
      new StaxDocumentLoader(resourceTable.getPackageName(), resourcePath.getResourceBase(),
          new NodeHandler()
              .addHandler("resources", new NodeHandler()
                  .addHandler("bool", new StaxValueLoader(resourceTable, "bool", ResType.BOOLEAN))
                  .addHandler("item[@type='bool']", new StaxValueLoader(resourceTable, "bool", ResType.BOOLEAN))
                  .addHandler("color", new StaxValueLoader(resourceTable, "color", ResType.COLOR))
                  .addHandler("drawable", new StaxValueLoader(resourceTable, "drawable", ResType.DRAWABLE))
                  .addHandler("item[@type='color']", new StaxValueLoader(resourceTable, "color", ResType.COLOR))
                  .addHandler("item[@type='drawable']", new StaxValueLoader(resourceTable, "drawable", ResType.DRAWABLE))
                  .addHandler("dimen", new StaxValueLoader(resourceTable, "dimen", ResType.DIMEN))
                  .addHandler("item[@type='dimen']", new StaxValueLoader(resourceTable, "dimen", ResType.DIMEN))
                  .addHandler("integer", new StaxValueLoader(resourceTable, "integer", ResType.INTEGER))
                  .addHandler("item[@type='integer']", new StaxValueLoader(resourceTable, "integer", ResType.INTEGER))
                  .addHandler("integer-array", new StaxArrayLoader(resourceTable, "array", ResType.INTEGER_ARRAY, ResType.INTEGER))
                  .addHandler("fraction", new StaxValueLoader(resourceTable, "fraction", ResType.FRACTION))
                  .addHandler("item[@type='fraction']", new StaxValueLoader(resourceTable, "fraction", ResType.FRACTION))
                  .addHandler("item[@type='layout']", new StaxValueLoader(resourceTable, "layout", ResType.LAYOUT))
                  .addHandler("plurals", new StaxPluralsLoader(resourceTable, "plurals", ResType.CHAR_SEQUENCE))
                  .addHandler("string", new StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE))
                  .addHandler("item[@type='string']", new StaxValueLoader(resourceTable, "string", ResType.CHAR_SEQUENCE))
                  .addHandler("string-array", new StaxArrayLoader(resourceTable, "array", ResType.CHAR_SEQUENCE_ARRAY, ResType.CHAR_SEQUENCE))
                  .addHandler("array", new StaxArrayLoader(resourceTable, "array", ResType.TYPED_ARRAY, null))
                  .addHandler("id", new StaxValueLoader(resourceTable, "id", ResType.CHAR_SEQUENCE))
                  .addHandler("item[@type='id']", new StaxValueLoader(resourceTable, "id", ResType.CHAR_SEQUENCE))
                  .addHandler("attr", new StaxAttrLoader(resourceTable, "attr", ResType.ATTR_DATA))
                  .addHandler("declare-styleable", new NodeHandler()
                      .addHandler("attr", new StaxAttrLoader(resourceTable, "attr", ResType.ATTR_DATA))
                  )
                  .addHandler("style", new StaxStyleLoader(resourceTable, "style", ResType.STYLE))
              )).load("values");

      loadOpaque(resourcePath, resourceTable, "layout", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "menu", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "drawable", ResType.DRAWABLE);
      loadOpaque(resourcePath, resourceTable, "anim", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "animator", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "color", ResType.COLOR_STATE_LIST);
      loadOpaque(resourcePath, resourceTable, "xml", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "transition", ResType.LAYOUT);
      loadOpaque(resourcePath, resourceTable, "interpolator", ResType.LAYOUT);

      new DrawableResourceLoader(resourceTable).findDrawableResources(resourcePath);
      new RawResourceLoader(resourcePath).loadTo(resourceTable);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void loadOpaque(ResourcePath resourcePath, final InMemoryPackageResourceTable resourceTable, final String type, final ResType resType) {
    new DocumentLoader(resourceTable.getPackageName(), resourcePath.getResourceBase()) {
      @Override
      protected void loadResourceXmlFile(XmlContext xmlContext) {
        resourceTable.addResource(type, xmlContext.getXmlFile().getBaseName(),
            new TypedResource(xmlContext.getXmlFile(), resType, xmlContext));
      }
    }.load(type);
  }
}