package org.robolectric.manifest;

import java.util.List;
import java.util.Map;

public class ContentProviderData extends PackageItemData {
  private static final String READ_PERMISSION = "android:readPermission";
  private static final String WRITE_PERMISSION = "android:writePermission";
  private static final String GRANT_URI_PERMISSION = "android:grantUriPermissions";
  private static final String ENABLED = "android:enabled";

  private final String authority;
  private final Map<String, String> attributes;
  private final List<PathPermissionData> pathPermissionDatas;

  public ContentProviderData(
      String className,
      MetaData metaData,
      String authority,
      Map<String, String> attributes,
      List<PathPermissionData> pathPermissionDatas) {
    super(className, metaData);
    this.authority = authority;
    this.attributes = attributes;
    this.pathPermissionDatas = pathPermissionDatas;
  }

  public String getAuthorities() {
    return authority;
  }

  public String getReadPermission() {
    return attributes.get(READ_PERMISSION);
  }

  public String getWritePermission() {
    return attributes.get(WRITE_PERMISSION);
  }

  public List<PathPermissionData> getPathPermissionDatas() {
    return pathPermissionDatas;
  }

  public boolean getGrantUriPermissions() {
    return Boolean.parseBoolean(attributes.get(GRANT_URI_PERMISSION));
  }

  public boolean isEnabled() {
    return attributes.containsKey(ENABLED) ? Boolean.parseBoolean(attributes.get(ENABLED)) : true;
  }
}
