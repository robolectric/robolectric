package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ServiceData {
  private final String className;
  private final MetaData metaData;
  private final List<String> actions;
  private String permission;
  private List<IntentFilterData> intentFilters;

  public ServiceData(String className, MetaData metaData, List<IntentFilterData> intentFilterData) {
    this.actions = new ArrayList<>();
    this.className = className;
    this.metaData = metaData;
    intentFilters = new LinkedList<>(intentFilterData);
  }

  public String getClassName() {
    return className;
  }

  public List<String> getActions() {
    return actions;
  }

  public MetaData getMetaData() {
    return metaData;
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

  /**
   * Get the intent filters defined for activity.
   * @return A list of intent filters. Not null.
   */
  public List<IntentFilterData> getIntentFilters() {
    return intentFilters;
  }
}
