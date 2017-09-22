package org.robolectric.manifest;

public class PathPermissionData {
  public final String path;
  public final String pathPrefix;
  public final String pathPattern;
  public final String readPermission;
  public final String writePermission;

  PathPermissionData(String path, String pathPrefix, String pathPattern, String readPermission, String writePermission) {
    this.path = path;
    this.pathPrefix = pathPrefix;
    this.pathPattern = pathPattern;
    this.readPermission = readPermission;
    this.writePermission = writePermission;
  }
}
