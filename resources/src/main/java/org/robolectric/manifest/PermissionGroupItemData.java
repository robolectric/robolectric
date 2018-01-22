package org.robolectric.manifest;

/**
 * Holds permission data from manifest.
 */
public class PermissionGroupItemData extends PackageItemData {

  private final String label;
  private final String description;

  public PermissionGroupItemData(String name, String label, String description,
      MetaData metaData) {
    super(name, metaData);

    this.label = label;
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }
}
