package org.robolectric.res;

import org.robolectric.util.Logger;

class ResourceParser {
  static void load(String packageName, ResourcePath resourcePath, PackageResourceTable resourceTable) {
    if (!resourcePath.hasResources()) {
      Logger.debug("No resources for %s", packageName);
      return;
    }

    Logger.debug("Loading resources for %s from %s...", packageName, resourcePath.getResourceBase());

    DocumentLoader documentLoader = new DocumentLoader(packageName, resourcePath);

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
          new ValueResourceLoader(resourceTable, "/resources/item", "layout", ResType.LAYOUT),
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

    new DrawableResourceLoader(packageName, resourceTable).findDrawableResources(resourcePath);
    new RawResourceLoader(packageName, resourcePath).loadTo(resourceTable);
  }
}
