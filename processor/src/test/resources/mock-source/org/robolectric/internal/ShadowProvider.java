package org.robolectric.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public interface ShadowProvider {

  void reset();

  String[] getProvidedPackageNames();

  Collection<Map.Entry<String, String>> getShadows();

  default Map<String, String> getShadowPickerMap() {
    return Collections.emptyMap();
  }
}
