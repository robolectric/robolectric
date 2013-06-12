package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.List;

public class OverlayResourceLoader extends XResourceLoader {
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

      pluralsData.mergeLibraryStyle(subResourceLoader.pluralsData, packageName);
      stringData.mergeLibraryStyle(subResourceLoader.stringData, packageName);
      menuData.mergeLibraryStyle(subResourceLoader.menuData, packageName);
      drawableData.mergeLibraryStyle(subResourceLoader.drawableData, packageName);
      preferenceData.mergeLibraryStyle(subResourceLoader.preferenceData, packageName);
      xmlDocuments.mergeLibraryStyle(subResourceLoader.xmlDocuments, packageName);
      rawResources.mergeLibraryStyle(subResourceLoader.rawResources, packageName);
      data.mergeLibraryStyle(subResourceLoader.data, packageName);
    }
  }

  @Override public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
    return super.getDrawableNode(resName.withPackageName(packageName), qualifiers);
  }

  @Override public MenuNode getMenuNode(ResName resName, String qualifiers) {
    return super.getMenuNode(resName.withPackageName(packageName), qualifiers);
  }

  @Override public Plural getPlural(ResName resName, int quantity, String qualifiers) {
    return super.getPlural(resName.withPackageName(packageName), quantity, qualifiers);
  }

  @Override public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
    return super.getPreferenceNode(resName.withPackageName(packageName), qualifiers);
  }

  @Override public InputStream getRawValue(ResName resName) {
    return super.getRawValue(resName.withPackageName(packageName));
  }

  @Override public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
    return super.getValue(resName.withPackageName(packageName), qualifiers);
  }

  @Override public Document getXml(ResName resName, String qualifiers) {
    return super.getXml(resName.withPackageName(packageName), qualifiers);
  }

  @Override public boolean providesFor(String namespace) {
    for (PackageResourceLoader subResourceLoader : subResourceLoaders) {
      if (subResourceLoader.providesFor(namespace)) {
        return true;
      }
    }
    return false;
  }
}
