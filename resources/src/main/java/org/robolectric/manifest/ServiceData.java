package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Holds parsed service data from manifest.
 */
public class ServiceData extends PackageItemData {

  private static final String EXPORTED = "android:exported";
  private static final String NAME = "android:name";
  private static final String PERMISSION = "android:permission";
  private static final String ENABLED = "android:enabled";

  private final Map<String, String> attributes;
  private final List<String> actions;
  private List<IntentFilterData> intentFilters;

  public ServiceData(
      Map<String, String> attributes, MetaData metaData, List<IntentFilterData> intentFilters) {
    super(attributes.get(NAME), metaData);
    this.attributes = attributes;
    this.actions = new ArrayList<>();
    this.intentFilters = new ArrayList<>(intentFilters);
  }

  public List<String> getActions() {
    return actions;
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

  public boolean isEnabled() {
    return attributes.containsKey(ENABLED) ? Boolean.parseBoolean(attributes.get(ENABLED)) : true;
  }
}
