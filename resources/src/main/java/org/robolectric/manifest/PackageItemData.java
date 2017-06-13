package org.robolectric.manifest;

public class PackageItemData {
  protected final String className;
  protected final MetaData metaData;

  public PackageItemData(String className, MetaData metaData) {
    this.metaData = metaData;
    this.className = className;
  }

  public String getClassName() {
    return className;
  }

  public MetaData getMetaData() {
    return metaData;
  }
}
