package org.robolectric.internal;

import java.util.Map;

public interface ShadowProvider {

  void reset();

  String[] getProvidedPackageNames();

  Map<String, String> getShadowMap();
}
