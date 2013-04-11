package org.robolectric.res;

import android.view.View;

import java.util.List;

public class OverlayResourceLoader extends XResourceLoader {
    public static final boolean DEBUG = true;

    private final String packageName;
    private final List<PackageResourceLoader> subResourceLoaders;

    public OverlayResourceLoader(String packageName, List<PackageResourceLoader> subResourceLoaders) {
        super(new OverlayResourceIndex(packageName, subResourceLoaders));
        this.packageName = packageName;
        this.subResourceLoaders = subResourceLoaders;
    }

    @Override
    void doInitialize() {
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
            subResourceLoader.initialize();

            booleanData.mergeLibraryStyle(subResourceLoader.booleanData, packageName);
            colorData.mergeLibraryStyle(subResourceLoader.colorData, packageName);
            dimenData.mergeLibraryStyle(subResourceLoader.dimenData, packageName);
            integerData.mergeLibraryStyle(subResourceLoader.integerData, packageName);
            pluralsData.mergeLibraryStyle(subResourceLoader.pluralsData, packageName);
            stringData.mergeLibraryStyle(subResourceLoader.stringData, packageName);
            layoutData.mergeLibraryStyle(subResourceLoader.layoutData, packageName);
            menuData.mergeLibraryStyle(subResourceLoader.menuData, packageName);
            drawableData.mergeLibraryStyle(subResourceLoader.drawableData, packageName);
            preferenceData.mergeLibraryStyle(subResourceLoader.preferenceData, packageName);
            xmlDocuments.mergeLibraryStyle(subResourceLoader.xmlDocuments, packageName);
            rawResourceFiles.mergeLibraryStyle(subResourceLoader.rawResourceFiles, packageName);
        }
    }

    @Override
    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute) {
        initialize();

        // todo: this sucks
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
            boolean b = subResourceLoader.hasAttributeFor(viewClass, namespace, attribute);
            if (b) return true;
        }
        return false;
    }

    @Override
    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part) {
        initialize();

        // todo: this sucks
        for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
            String s = subResourceLoader.convertValueToEnum(viewClass, namespace, attribute, part);
            if (s != null) return s;
        }
        return null;
    }
}
