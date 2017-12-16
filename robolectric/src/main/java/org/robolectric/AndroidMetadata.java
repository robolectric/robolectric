package org.robolectric;

import java.util.Map;

public class AndroidMetadata {

  private Map<String, String> deviceBootProperties;

  public AndroidMetadata(Map<String, String> deviceBootProperties) {
    this.deviceBootProperties = deviceBootProperties;
  }

  public Map<String, String> getDeviceBootProperties() {
    return deviceBootProperties;
  }
}
