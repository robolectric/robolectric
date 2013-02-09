package org.robolectric.res;

import org.robolectric.util.I18nException;
import org.w3c.dom.Document;

public class PackageResourceLoader extends XResourceLoader {
    ResourcePath resourcePath;

    public PackageResourceLoader(ResourcePath resourcePath) {
        this.resourcePath = resourcePath;
    }

    void init() {
        if (isInitialized) return;

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
                new ValueResourceLoader(booleanResolver, "bool", false),
                new ValueResourceLoader(colorResolver, "color", false),
                new ValueResourceLoader(dimenResolver, "dimen", false),
                new ValueResourceLoader(integerResolver, "integer", true),
                new PluralResourceLoader(resourceIndex, pluralsResolver),
                new ValueResourceLoader(stringResolver, "string", true),
                new AttrResourceLoader()
        ).loadResourceXmlSubDirs(resourcePath, "values");

        new DocumentLoader(new ViewLoader(viewNodes)).loadResourceXmlSubDirs(resourcePath, "layout");
        new DocumentLoader(new MenuLoader(menuNodes)).loadResourceXmlSubDirs(resourcePath, "menu");
        DrawableResourceLoader drawableResourceLoader = new DrawableResourceLoader(drawableNodes);
        drawableResourceLoader.findNinePatchResources(resourcePath);
        new DocumentLoader(drawableResourceLoader).loadResourceXmlSubDirs(resourcePath, "drawable");
        new DocumentLoader(new PreferenceLoader(resourceIndex)).loadResourceXmlSubDirs(resourcePath, "xml");
        new DocumentLoader(new XmlFileLoader(resourceIndex)).loadResourceXmlSubDirs(resourcePath, "xml");

        loadOtherResources(resourcePath);

        rawResourceLoaders.add(new RawResourceLoader(resourceIndex, resourcePath.resourceBase));

        isInitialized = true;
    }

    protected void loadOtherResources(ResourcePath resourcePath) {
    }
}
