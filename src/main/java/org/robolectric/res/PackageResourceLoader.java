package org.robolectric.res;

import org.robolectric.util.I18nException;

import java.io.File;

public class PackageResourceLoader extends XResourceLoader {
  ResourcePath resourcePath;

  public PackageResourceLoader(ResourcePath resourcePath) {
    this(resourcePath, new ResourceExtractor(resourcePath));
  }

  public PackageResourceLoader(ResourcePath resourcePath, ResourceIndex resourceIndex) {
    super(resourceIndex);
    this.resourcePath = resourcePath;
    String separator = resourcePath.packageName.equals("android") ? "/" : File.separator;
    if (!resourcePath.resourceBase.toString().endsWith(separator + "res"))
    {
      throw new IllegalArgumentException("Resource path must end in \"" + separator + "res\"");
    }
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
    //android package is loaded from jar. jars always have "/" separator
    //should probably check if the resourcePath is referencing a jar instead, but not sure how to
    String separator = resourcePath.packageName.equals("android") ? "/" : File.separator;

    documentLoader.load("res" + separator + "values",
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
    documentLoader.load("res" + separator + "layout", new OpaqueFileLoader(data, "layout"), new XmlFileLoader(xmlDocuments, "layout"));
    documentLoader.load("res" + separator + "menu", new MenuLoader(menuData), new XmlFileLoader(xmlDocuments, "menu"));
    documentLoader.load("res" + separator + "drawable", new OpaqueFileLoader(data, "drawable"), new XmlFileLoader(xmlDocuments, "drawable"));
    documentLoader.load("res" + separator + "anim", new OpaqueFileLoader(data, "anim"), new XmlFileLoader(xmlDocuments, "anim"));
    documentLoader.load("res" + separator + "color", new ColorResourceLoader(data), new XmlFileLoader(xmlDocuments, "color"));
    documentLoader.load("res" + separator + "xml", new PreferenceLoader(preferenceData), new XmlFileLoader(xmlDocuments, "xml"));
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
