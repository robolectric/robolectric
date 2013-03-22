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

            booleanResolver.mergeLibraryStyle(subResourceLoader.booleanResolver, packageName);
            colorResolver.mergeLibraryStyle(subResourceLoader.colorResolver, packageName);
            dimenResolver.mergeLibraryStyle(subResourceLoader.dimenResolver, packageName);
            integerResolver.mergeLibraryStyle(subResourceLoader.integerResolver, packageName);
            pluralsResolver.mergeLibraryStyle(subResourceLoader.pluralsResolver, packageName);
            stringResolver.mergeLibraryStyle(subResourceLoader.stringResolver, packageName);
            viewNodes.mergeLibraryStyle(subResourceLoader.viewNodes, packageName);
            menuNodes.mergeLibraryStyle(subResourceLoader.menuNodes, packageName);
            drawableNodes.mergeLibraryStyle(subResourceLoader.drawableNodes, packageName);
            preferenceNodes.mergeLibraryStyle(subResourceLoader.preferenceNodes, packageName);
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
