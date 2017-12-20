package org.robolectric.manifest;

public class PackageItemData {
  protected final String name;
  protected final MetaData metaData;

  public PackageItemData(String name, MetaData metaData) {
    this.metaData = metaData;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  /** @deprecated - Use {@link #getName()} instead. */
  @Deprecated
  public String getClassName() {
    return getName();
  }

  public MetaData getMetaData() {
    return metaData;
  }
}
