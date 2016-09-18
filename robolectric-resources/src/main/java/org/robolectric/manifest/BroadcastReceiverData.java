package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.List;

public class BroadcastReceiverData extends PackageItemData {
  private final List<String> actions;
  private String permission;

  public BroadcastReceiverData(String className, MetaData metaData) {
    super(className, metaData);
    this.actions = new ArrayList<>();
  }

  public List<String> getActions() {
    return actions;
  }

  public void addAction(String action) {
    this.actions.add(action);
  }

  public void setPermission(final String permission) {
    this.permission = permission;
  }

  public String getPermission() {
    return permission;
  }
}
