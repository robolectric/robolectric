package org.robolectric.manifest;

import java.util.List;

public class ContentProviderData extends PackageItemData {
  private final String authority;
  private final String readPermission;
  private final String writePermission;
  private final List<PathPermissionData> pathPermissionDatas;
  private final boolean grantUriPermissions;

  public ContentProviderData(
      String className,
      MetaData metaData,
      String authority,
      String readPermission,
      String writePermission,
      List<PathPermissionData> pathPermissionDatas,
      String grantUriPermissions) {
    super(className, metaData);
    this.authority = authority;
    this.readPermission = readPermission;
    this.writePermission = writePermission;
    this.pathPermissionDatas = pathPermissionDatas;
    this.grantUriPermissions = Boolean.parseBoolean(grantUriPermissions);
  }

  public String getAuthorities() {
    return authority;
  }

  public String getReadPermission() {
    return readPermission;
  }

  public String getWritePermission() {
    return writePermission;
  }

  public List<PathPermissionData> getPathPermissionDatas() {
    return pathPermissionDatas;
  }

  public boolean getGrantUriPermissions() {
    return grantUriPermissions;
  }
}
