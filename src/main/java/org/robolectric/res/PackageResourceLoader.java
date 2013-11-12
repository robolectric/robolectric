package org.robolectric.res;

import org.robolectric.util.I18nException;

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
    } catch (I18nException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void loadEverything() throws Exception {
    System.out.println("DEBUG: Loading resources for " + resourcePath.getPackageName() + " from " + resourcePath.resourceBase + "...");

    DocumentLoader documentLoader = new DocumentLoader(resourcePath);

    documentLoader.load("values",
        new ValueResourceLoader(data, "/resources/bool", "bool", ResType.BOOLEAN),
        new ValueResourceLoader(data, "/resources/color", "color", ResType.COLOR),
        new ValueResourceLoader(data, "/resources/dimen", "dimen", ResType.DIMEN),
        new ValueResourceLoader(data, "/resources/integer", "integer", ResType.INTEGER),
        new ValueResourceLoader(data, "/resources/integer-array", "array", ResType.INTEGER_ARRAY),
        new PluralResourceLoader(pluralsData),
        new ValueResourceLoader(data, "/resources/string", "string", ResType.CHAR_SEQUENCE),
        new ValueResourceLoader(data, "/resources/string-array", "array", ResType.CHAR_SEQUENCE_ARRAY),
        new AttrResourceLoader(data),
        new StyleResourceLoader(data)
    );

    documentLoader.load("layout", new OpaqueFileLoader(data, "layout"), new XmlFileLoader(xmlDocuments, "layout"));
    documentLoader.load("menu", new MenuLoader(menuData), new XmlFileLoader(xmlDocuments, "menu"));
    documentLoader.load("drawable", new OpaqueFileLoader(data, "drawable"), new XmlFileLoader(xmlDocuments, "drawable"));
    documentLoader.load("anim", new OpaqueFileLoader(data, "anim"), new XmlFileLoader(xmlDocuments, "anim"));
    documentLoader.load("animator", new OpaqueFileLoader(data, "animator"), new XmlFileLoader(xmlDocuments, "animator"));
    documentLoader.load("color", new ColorResourceLoader(data), new XmlFileLoader(xmlDocuments, "color"));
    documentLoader.load("xml", new PreferenceLoader(preferenceData), new XmlFileLoader(xmlDocuments, "xml"));

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
