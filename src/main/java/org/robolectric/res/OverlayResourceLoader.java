package org.robolectric.res;

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
            subResourceLoader.doInitialize();

            booleanResolver.mergeLibraryStyle(subResourceLoader.booleanResolver, packageName);
            colorResolver.mergeLibraryStyle(subResourceLoader.colorResolver, packageName);
            dimenResolver.mergeLibraryStyle(subResourceLoader.dimenResolver, packageName);
            integerResolver.mergeLibraryStyle(subResourceLoader.integerResolver, packageName);
            pluralsResolver.mergeLibraryStyle(subResourceLoader.pluralsResolver, packageName);
            stringResolver.mergeLibraryStyle(subResourceLoader.stringResolver, packageName);
            viewNodes.mergeLibraryStyle(subResourceLoader.viewNodes, packageName);
            menuNodes.mergeLibraryStyle(subResourceLoader.menuNodes, packageName);
            drawableNodes.mergeLibraryStyle(subResourceLoader.drawableNodes, packageName);
        }
    }

}
