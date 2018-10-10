package org.robolectric;

import java.util.Map;

/**
 * Data related to Android tests.
 */
public class AndroidMetadata {

  private final Map<String, String> deviceBootProperties;
  private final String resourcesMode;

  public AndroidMetadata(Map<String, String> deviceBootProperties, String resourcesMode) {
    this.deviceBootProperties = deviceBootProperties;
    this.resourcesMode = resourcesMode;
  }

  public Map<String, String> getDeviceBootProperties() {
    return deviceBootProperties;
  }

  public String getResourcesMode() {
    return resourcesMode;
  }
}
