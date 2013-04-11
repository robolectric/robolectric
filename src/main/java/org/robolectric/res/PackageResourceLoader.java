package org.robolectric.res;

import org.robolectric.util.I18nException;

public class PackageResourceLoader extends XResourceLoader {
    ResourcePath resourcePath;
    ResourceIndex resourceIndex;

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
        documentLoader.loadResourceXmlSubDirs("values",
                new ValueResourceLoader(booleanData, "bool", false),
                new ValueResourceLoader(colorData, "color", false),
                new ValueResourceLoader(dimenData, "dimen", false),
                new ValueResourceLoader(integerData, "integer", true),
                new PluralResourceLoader(resourceIndex, pluralsData),
                new ValueResourceLoader(stringData, "string", true),
                attrResourceLoader
        );

        documentLoader.loadResourceXmlSubDirs("layout", new ViewLoader(layoutData));
        documentLoader.loadResourceXmlSubDirs("menu", new MenuLoader(menuData));
        DrawableResourceLoader drawableResourceLoader = new DrawableResourceLoader(drawableData);
        drawableResourceLoader.findNinePatchResources(resourcePath);
        documentLoader.loadResourceXmlSubDirs("drawable", drawableResourceLoader);
        documentLoader.loadResourceXmlSubDirs("xml", new PreferenceLoader(preferenceData));
        documentLoader.loadResourceXmlSubDirs("xml", new XmlFileLoader(xmlDocuments));
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
}
