package org.robolectric;

import java.util.Map;

/** Data related to Android tests. */
public class AndroidMetadata {

  private final Map<String, String> deviceBootProperties;

  public AndroidMetadata(Map<String, String> deviceBootProperties) {
    this.deviceBootProperties = deviceBootProperties;
  }

  public Map<String, String> getDeviceBootProperties() {
    return deviceBootProperties;
  }

}
