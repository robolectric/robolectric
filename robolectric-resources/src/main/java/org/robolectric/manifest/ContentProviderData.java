package org.robolectric.manifest;

public class ContentProviderData extends PackageItemData {
  private final String authority;

  public ContentProviderData(String className, MetaData metaData, String authority) {
    super(className, metaData);
    this.authority = authority;
  }

  public String getAuthority() {
    return authority;
  }
}
