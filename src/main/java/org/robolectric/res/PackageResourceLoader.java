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

        new DocumentLoader(
                new ValueResourceLoader(booleanData, "bool", false),
                new ValueResourceLoader(colorData, "color", false),
                new ValueResourceLoader(dimenData, "dimen", false),
                new ValueResourceLoader(integerData, "integer", true),
                new PluralResourceLoader(resourceIndex, pluralsData),
                new ValueResourceLoader(stringData, "string", true),
                attrResourceLoader
        ).loadResourceXmlSubDirs(resourcePath, "values");

        new DocumentLoader(new ViewLoader(layoutData)).loadResourceXmlSubDirs(resourcePath, "layout");
        new DocumentLoader(new MenuLoader(menuData)).loadResourceXmlSubDirs(resourcePath, "menu");
        DrawableResourceLoader drawableResourceLoader = new DrawableResourceLoader(drawableData);
        drawableResourceLoader.findNinePatchResources(resourcePath);
        new DocumentLoader(drawableResourceLoader).loadResourceXmlSubDirs(resourcePath, "drawable");
        new DocumentLoader(new PreferenceLoader(preferenceData)).loadResourceXmlSubDirs(resourcePath, "xml");
        new DocumentLoader(new XmlFileLoader(xmlDocuments)).loadResourceXmlSubDirs(resourcePath, "xml");
        new RawResourceLoader(rawResourceFiles).loadFrom(resourcePath);

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
