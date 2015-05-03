package org.robolectric.res;

import org.robolectric.util.Logger;

public class PackageResourceLoader extends XResourceLoader {
  private final ResourcePath resourcePath;

  public PackageResourceLoader(ResourcePath resourcePath) {
    this(resourcePath, new ResourceExtractor(resourcePath));
  }

  public PackageResourceLoader(ResourcePath resourcePath, ResourceIndex resourceIndex) {
    super(resourceIndex);
    this.resourcePath = resourcePath;
  }

  void doInitialize() {
    try {
      loadEverything();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void loadEverything() throws Exception {
    Logger.debug("Loading resources for %s from %s...", resourcePath.getPackageName(), resourcePath.resourceBase);

    DocumentLoader documentLoader = new DocumentLoader(resourcePath);

    documentLoader.load("values",
        new ValueResourceLoader(data, "/resources/bool", "bool", ResType.BOOLEAN),
        new ValueResourceLoader(data, "/resources/item[@type='bool']", "bool", ResType.BOOLEAN),
        new ValueResourceLoader(data, "/resources/color", "color", ResType.COLOR),
        new ValueResourceLoader(data, "/resources/drawable", "drawable", ResType.DRAWABLE),
        new ValueResourceLoader(data, "/resources/item[@type='color']", "color", ResType.COLOR),
        new ValueResourceLoader(data, "/resources/dimen", "dimen", ResType.DIMEN),
        new ValueResourceLoader(data, "/resources/item[@type='dimen']", "dimen", ResType.DIMEN),
        new ValueResourceLoader(data, "/resources/integer", "integer", ResType.INTEGER),
        new ValueResourceLoader(data, "/resources/item[@type='integer']", "integer", ResType.INTEGER),
        new ValueResourceLoader(data, "/resources/integer-array", "array", ResType.INTEGER_ARRAY),
        new ValueResourceLoader(data, "/resources/fraction", "fraction", ResType.FRACTION),
        new ValueResourceLoader(data, "/resources/item[@type='fraction']", "fraction", ResType.FRACTION),
        new ValueResourceLoader(data, "/resources/item", "layout", ResType.LAYOUT),
        new PluralResourceLoader(pluralsData),
        new ValueResourceLoader(data, "/resources/string", "string", ResType.CHAR_SEQUENCE),
        new ValueResourceLoader(data, "/resources/item[@type='string']", "string", ResType.CHAR_SEQUENCE),
        new ValueResourceLoader(data, "/resources/string-array", "array", ResType.CHAR_SEQUENCE_ARRAY),
        new AttrResourceLoader(data),
        new StyleResourceLoader(data)
    );

    documentLoader.load("layout", new OpaqueFileLoader(data, "layout"), new XmlBlockLoader(xmlDocuments, "layout"));
    documentLoader.load("menu", new OpaqueFileLoader(data, "menu"), new XmlBlockLoader(xmlDocuments, "menu"));
    documentLoader.load("drawable", new OpaqueFileLoader(data, "drawable"), new XmlBlockLoader(xmlDocuments, "drawable"));
    documentLoader.load("anim", new OpaqueFileLoader(data, "anim"), new XmlBlockLoader(xmlDocuments, "anim"));
    documentLoader.load("animator", new OpaqueFileLoader(data, "animator"), new XmlBlockLoader(xmlDocuments, "animator"));
    documentLoader.load("color", new ColorResourceLoader(data), new XmlBlockLoader(xmlDocuments, "color"));
    documentLoader.load("xml", new PreferenceLoader(preferenceData), new XmlBlockLoader(xmlDocuments, "xml"));
    documentLoader.load("transition", new OpaqueFileLoader(data, "transition"), new XmlBlockLoader(xmlDocuments, "transition"));
    documentLoader.load("interpolator", new OpaqueFileLoader(data, "interpolator"), new XmlBlockLoader(xmlDocuments, "interpolator"));

    new DrawableResourceLoader(drawableData).findDrawableResources(resourcePath);
    new RawResourceLoader(resourcePath).loadTo(rawResources);

    loadOtherResources(resourcePath);
  }

  protected void loadOtherResources(ResourcePath resourcePath) {
  }

  @Override
  public String toString() {
    return "PackageResourceLoader{" +
        "resourcePath=" + resourcePath +
        '}';
  }

  @Override public boolean providesFor(String namespace) {
    return resourcePath.getPackageName().equals(namespace);
  }
}
