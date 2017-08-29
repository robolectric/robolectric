package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServiceData {

  private static final String EXPORTED = "android:exported";
  private static final String NAME = "android:name";
  private static final String PERMISSION = "android:permission";

  private final Map<String, String> attributes;
  private final MetaData metaData;
  private final List<String> actions;
  private List<IntentFilterData> intentFilters;

  public ServiceData(
      Map<String, String> attributes, MetaData metaData, List<IntentFilterData> intentFilters) {
    this.attributes = attributes;
    this.actions = new ArrayList<>();
    this.metaData = metaData;
    this.intentFilters = new LinkedList<>(intentFilters);
  }

  public ServiceData(String className, MetaData metaData, List<IntentFilterData> intentFilterData) {
    this.attributes = new HashMap<>();
    this.attributes.put(NAME, className);
    this.actions = new ArrayList<>();
    this.metaData = metaData;
    intentFilters = new LinkedList<>(intentFilterData);
  }

  public String getClassName() {
    return attributes.get(NAME);
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
    attributes.put(PERMISSION, permission);
  }

  public String getPermission() {
    return attributes.get(PERMISSION);
  }

  /**
   * Get the intent filters defined for the service.
   *
   * @return A list of intent filters.
   */
  public List<IntentFilterData> getIntentFilters() {
    return intentFilters;
  }

  /**
   * Get the map for all attributes defined for the service.
   *
   * @return map of attributes names to values from the manifest.
   */
  public Map<String, String> getAllAttributes() {
    return attributes;
  }

  /**
   * Returns whether this service is exported by checking the XML attribute.
   *
   * @return true if the service is exported
   */
  public boolean isExported() {
    boolean defaultValue = !intentFilters.isEmpty();
    return (attributes.containsKey(EXPORTED)
        ? Boolean.parseBoolean(attributes.get(EXPORTED))
        : defaultValue);
  }
}
