package org.robolectric.manifest;

/**
 * Holds permission data from manifest.
 */
public class PermissionItemData extends PackageItemData {

  private final String label;
  private final String description;
  private final String permissionGroup;
  private final String protectionLevel;

  public PermissionItemData(String name, String label, String description,
      String permissionGroup, String protectionLevel, MetaData metaData) {
    super(name, metaData);

    this.label = label;
    this.description = description;
    this.permissionGroup = permissionGroup;
    this.protectionLevel = protectionLevel;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

  public String getPermissionGroup() {
    return permissionGroup;
  }

  public String getProtectionLevel() {
    return protectionLevel;
  }
}
